package gradle_practice_20250712;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class Client {

    // アドレスとポート番号
    private String addr;
    private int port;

    public Client(String addr, int port) {
    	this.addr = addr;
    	this.port = port;
    }

    public void connect() {
        try (
            Socket socket = new Socket(addr, port);
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
