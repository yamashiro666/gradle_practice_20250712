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

        	// welcomeメッセージを受信する
            String welcomeMsg = in.readLine();
            if (welcomeMsg != null) {
                System.out.println("サーバに接続しました。: " + welcomeMsg);
            }

            System.out.println("メッセージを入力してください（byeで終了）:");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput); // サーバへ送信

                // サーバから複数行の応答を受け取る（nullまで or ENDまで）
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    if ("END".equals(responseLine)) break; // ← 必要ならここで終了条件
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
