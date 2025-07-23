package gradle_practice_20250712;

import java.util.Map;

public class Test {

	@org.junit.jupiter.api.Test
	public void test1() {

    	// プロパティ情報を取得
    	Map<Integer, String> map = ConfigLoader.loadPortFileMap();

//    	// テスト出力
//    	map.forEach((k,v) -> {
//    		System.out.println("Key: " + k + " " + "Value: " + v);
//    	});

    	Client c = new Client("localhost", 5001, map);
    	c.connect();




	}
}
