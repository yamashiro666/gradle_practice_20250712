package gradle_practice_20250712;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Optional;

public class Client {

    // ベースパス
    private static final String BASE_PATH = "src/main/resources/client_file";

    // アドレスとポート番号,設定情報
    private String addr;
    private int port;
    private Map<Integer, String> map;

    public Client(String addr, int port, Map<Integer, String> map) {
    	this.addr = addr;
    	this.port = port;
    	this.map = map;
    }

    public void connect() {

    	// mapからファイル名を取得。ベースのパスも一緒に付与する。
    	Optional<String> mayBefilePath = map.entrySet().stream()
    		    .filter(e -> e.getKey() == port)
    		    .map(e -> BASE_PATH + "/" + e.getValue())
    		    .findFirst();

    	// パスが取得できない場合は、エラーを表示。
    	if(!mayBefilePath.isPresent()) {
    		System.out.println("該当ポートが見つかりません");
    	}

        try (
            Socket socket = new Socket(addr, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    		BufferedReader fileReader = new BufferedReader(new FileReader(mayBefilePath.get()))
        ) {

        	// welcomeメッセージを受信する
            String welcomeMsg = in.readLine();
            if (welcomeMsg != null) {
            	System.out.println("======================================");
                System.out.println("サーバに接続しました。: " + welcomeMsg);
                System.out.println("ファイル送信処理を開始します。");
            } else {
            	throw new IOException("サーバからのウェルカムメッセージが null");
            }

            // ファイルから1行ずつ読み取って送信
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                out.println(fileLine); // サーバへ送信
            }

            // 終了コードを送信
            out.println("exit");

        } catch (IOException e) {
            System.err.println("クライアントでエラー: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("ファイルの送信が完了しました。");
        System.out.println("======================================");
    }
}
