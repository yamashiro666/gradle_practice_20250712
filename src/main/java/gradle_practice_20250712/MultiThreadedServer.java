package gradle_practice_20250712;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiThreadedServer {

    private static final int THREADS_PER_POOL = 10;
    private static final String basepath = System.getProperty("user.dir");
    private static final String CONFIG_FILE_PATH = basepath + "/src/main/java/gradle_practice_20250712/ports.properties";
    private static final List<ExecutorService> threadPools = new ArrayList<>();

    public static void main(String[] args) {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("サーバーでエラーが発生しました: " + e.getMessage());
            System.out.println(e);
            return;
        }

        // ポート番号を収集
        List<Integer> ports = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            ports.add(Integer.parseInt(props.getProperty(key)));
        }

        // 各ポートに対してスレッドプールとサーバーソケットを作成
        for (int i = 0; i < ports.size(); i++) {
            int port = ports.get(i);
            ExecutorService pool = Executors.newFixedThreadPool(THREADS_PER_POOL);
            threadPools.add(pool);

            final int index = i;
            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    System.out.println("ポート " + port + " で待ち受け開始");

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        pool.execute(new ClientHandler(clientSocket, port, index));
                    }

                } catch (IOException e) {
                    System.err.println("ポート " + port + " での待ち受け中にエラー: " + e.getMessage());
                }
            }).start();
        }
    }

    // クライアント処理用スレッド
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int port;
        private final int poolIndex;

        public ClientHandler(Socket socket, int port, int poolIndex) {
            this.socket = socket;
            this.port = port;
            this.poolIndex = poolIndex;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {

                out.println("接続成功。 Serverポート: " + port + "（最大接続数: " + poolIndex + "）");

                // コンソールに接続情報を表示する。
                System.out.println("========================================");
                System.out.println("クライアントから接続がありました。");
                System.out.println("Clientアドレス: " + socket.getRemoteSocketAddress());
                System.out.println("Clientポート番号: " + socket.getPort());
                System.out.println("Serverポート番号: " + socket.getLocalPort());
                System.out.println("========================================");

                // 受信したメッセージを処理する
                String line;
                while ((line = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(line)) break;
                    out.println("Echo: " + line);
                    System.out.println("受信メッセージ: " + line);

                    // クライアント側に終了メッセージを送る
                    out.println("END");
                }

            } catch (IOException e) {
                System.err.println("クライアント処理エラー: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignore) {}
            }
        }
    }
}
