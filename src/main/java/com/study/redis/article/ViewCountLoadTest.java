package com.study.redis.article;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewCountLoadTest {

    private static final String BASE_URL = "http://localhost:8080/articles/1";
    private static final int REQUEST_COUNT = 100;

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        CountDownLatch startGate = new CountDownLatch(1);   // 100개를 동시에 출발시키는 장치

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < REQUEST_COUNT; i++) {
                executor.submit(new ViewRequest(client, startGate));
            }
            startGate.countDown();
        } // 모든 요청이 끝날 때까지 대기

        HttpRequest statsRequest = HttpRequest.newBuilder(URI.create(BASE_URL + "/stats")).GET().build();
        System.out.println("📊 최종 결과: " + client.send(statsRequest, HttpResponse.BodyHandlers.ofString()).body());
    }

    /** 조회수 증가 요청 1건 */
    private static class ViewRequest implements Runnable {

        private final HttpClient client;
        private final CountDownLatch startGate;

        ViewRequest(HttpClient client, CountDownLatch startGate) {
            this.client = client;
            this.startGate = startGate;
        }

        @Override
        public void run() {
            try {
                startGate.await();
                HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/view"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                System.out.println("❌ 에러: " + e);
            }
        }
    }
}