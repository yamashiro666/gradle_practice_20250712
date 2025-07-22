package gradle_practice_20250712;

public class Test {

	String basepath = System.getProperty("user.dir");

	@org.junit.jupiter.api.Test
	public void test1() {
		System.out.println(basepath + "/src/main/java/gradle_practice_20250712/ports.properties");
	}
}
