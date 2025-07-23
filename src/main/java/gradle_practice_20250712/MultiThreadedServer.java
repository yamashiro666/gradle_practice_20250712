package gradle_practice_20250712;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadedServer {

	// 最大接続数
    private static final int THREADS_PER_POOL = 10;

    // ポートごとの接続数カウンタ（スレッドセーフ）
    private static final Map<Integer, AtomicInteger> connectionCounters = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        // 設定ファイルからポートとファイル名のマッピングを取得
        Map<Integer, String> portFileMap = ConfigLoader.loadPortFileMap();
        if (portFileMap.isEmpty()) {
            System.err.println("設定ファイルに有効なポートが定義されていません。");
            return;
        }

        // ポートごとに10個保持できるスレッドプールを作成する。
        for (Map.Entry<Integer, String> entry : portFileMap.entrySet()) {
            int port = entry.getKey();
            String fileName = entry.getValue();

            // 接続数カウンタを初期化
            connectionCounters.put(port, new AtomicInteger(0));

            ExecutorService pool = Executors.newFixedThreadPool(THREADS_PER_POOL);

            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    System.out.println("ポート " + port + " で待ち受け開始。対応ファイル: " + fileName);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        pool.execute(new ClientHandler(clientSocket, port));
                    }
                } catch (IOException e) {
                    System.err.println("ポート " + port + " での待ち受けエラー: " + e.getMessage());
                }
            }).start();
        }

    }

    // クライアント処理用スレッド
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int port;

        public ClientHandler(Socket socket, int port) {
            this.socket = socket;
            this.port = port;
        }

        @Override
        public void run() {

        	int connectionCount = connectionCounters.get(port).incrementAndGet(); // 接続数をインクリメント

            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {

                out.println("接続成功。 Serverポート: " + port + "（接続数: " + connectionCounters.get(port).get() + "）");

                // コンソールに接続情報を表示する。
                System.out.println("========================================");
                System.out.println("クライアントから接続がありました。");
                System.out.println("Clientアドレス: " + socket.getRemoteSocketAddress());
                System.out.println("Clientポート番号: " + socket.getPort());
                System.out.println("Serverポート番号: " + socket.getLocalPort());
                System.out.println("現在の接続数: " + connectionCount);
                System.out.println("========================================");

                // 受信したメッセージを処理する
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(line)) break;

                    sb.append(line);

//                    out.println("Echo: " + line);
//                    System.out.println("受信メッセージ: " + line);

                    // クライアント側に終了メッセージを送る
//                    out.println("END");
                }

                System.out.println(sb.toString());

                Thread.sleep(50000);

                out.println("END");

            } catch (IOException e) {
                System.err.println("クライアント処理エラー: " + e.getMessage());
            } catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} finally {
            	connectionCounters.get(port).decrementAndGet(); // 切断後に接続数を戻す
                try {
                    socket.close();
                } catch (IOException ignore) {}
            }
        }
    }
}
