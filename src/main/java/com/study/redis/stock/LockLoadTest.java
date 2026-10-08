package com.study.redis.stock;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LockLoadTest {

    // 테스트할 주소: /jpa/stock/reduce, /mybatis/stock/reduce, /jpa/stock/reduce-unsafe
    private static final String URL = "http://localhost:8080/jpa/stock/reduce/100";

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        CountDownLatch startGate = new CountDownLatch(1);   // 10명을 동시에 출발시키는 장치
        long startTime = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 10; i++) {
                int reqNum = i;
                executor.submit(() -> {
                    try {
                        startGate.await();
                        System.out.println("🚀 요청 " + reqNum + " 출발!");
                        HttpRequest request = HttpRequest.newBuilder(URI.create(URL + "?userId=User_" + reqNum))
                                .timeout(Duration.ofSeconds(30))
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        System.out.println("✅ 요청 " + reqNum + " 결과: [" + response.statusCode() + "] " + response.body());
                    } catch (Exception e) {
                        System.out.println("❌ 요청 " + reqNum + " 에러: " + e);
                    }
                });
            }
            startGate.countDown();   // 동시에 출발
        } // 모든 요청이 끝날 때까지 대기 (asyncio.gather 역할)

        System.out.printf("⏰ 총 소요 시간: %.2f초%n", (System.currentTimeMillis() - startTime) / 1000.0);
    }
}