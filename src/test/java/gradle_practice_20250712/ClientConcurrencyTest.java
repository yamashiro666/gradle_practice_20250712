package gradle_practice_20250712;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientConcurrencyTest {

    private static final String SERVER_ADDR = "localhost";
    private static final int THREAD_COUNT = 15;

    @Test
    public void connectToAllPort() throws InterruptedException {
        Map<Integer, String> portFileMap = ConfigLoader.loadPortFileMap();

        List<Thread> portThreads = new ArrayList<>();

        for (Integer port : portFileMap.keySet()) {
            Thread portThread = new Thread(() -> {
                try {
                    connectToPort(port, portFileMap);
                } catch (InterruptedException e) {
                    System.err.println("ポート " + port + " でエラー: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }, "Port-Thread-" + port);
            portThreads.add(portThread);
            portThread.start();
        }

        for (Thread t : portThreads) {
            t.join();
        }

        System.out.println("全ポートに対する全クライアントスレッドが完了しました。");
    }

    public void connectToPort(int port, Map<Integer, String> portFileMap) throws InterruptedException {
        assertTrue(portFileMap.containsKey(port), "指定されたポートが設定ファイルに存在しません: " + port);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread clientThread = new Thread(() -> {
                Client client = new Client(SERVER_ADDR, port, portFileMap);
                client.connect();
            }, "Client-Thread-" + port + "-" + i);
            threads.add(clientThread);
            clientThread.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("ポート " + port + " に対する全クライアントスレッドが完了しました。");
    }
}
