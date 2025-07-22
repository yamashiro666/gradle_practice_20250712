package gradle_practice_20250712;
import java.io.*;
import java.net.*;

public class MultiThreadedServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("サーバー起動中... ポート番号: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // クライアントからの接続を待ち受け
                Socket clientSocket = serverSocket.accept();

                // 新しいスレッドを生成して接続を処理
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("サーバーでエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // クライアント接続を処理するクラス
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("クライアント接続: " + socket.getRemoteSocketAddress());

            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("受信: " + inputLine);
                    out.println("受け取りました: " + inputLine);

                    // 終了条件
                    if ("bye".equalsIgnoreCase(inputLine)) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("クライアント処理中にエラー: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("クライアント切断: " + socket.getRemoteSocketAddress());
                } catch (IOException e) {
                    // 無視
                }
            }
        }
    }
}
