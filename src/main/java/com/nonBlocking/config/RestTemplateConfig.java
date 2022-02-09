package com.nonBlocking.config;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfig {
    // 최대 Connection
    private int connection = 100;

    // ip, port 1쌍에 대해 수행할 Connection
    private int connectionPerRoute = 5;

    // 읽기 시간 초과 (ms)
    private int readTimeOut = 5000;

    // 연결 시간 초과 (ms)
    private int connectionTimeOut = 5000;

    @Bean
    public HttpClient httpClient() {
        return HttpClientBuilder.create()
                .setMaxConnTotal(connection)
                .setMaxConnPerRoute(connectionPerRoute)
                .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory factory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeOut);
        factory.setConnectTimeout(connectionTimeOut);
        factory.setHttpClient(httpClient);

        return factory;
    }

    @Bean
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }
}
