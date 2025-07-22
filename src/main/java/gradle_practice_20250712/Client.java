package gradle_practice_20250712;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // または "127.0.0.1"
    private static final int SERVER_PORT = 5001;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {

            System.out.println("サーバに接続しました。メッセージを入力してください（byeで終了）:");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput); // サーバへ送信

                String responseLine; // サーバから受信するメッセージを格納する用の変数
                while ((responseLine = in.readLine()) != null) {

                	// サーバからのメッセージを表示
                    System.out.println("サーバから: " + responseLine);
                }

                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("クライアントでエラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
