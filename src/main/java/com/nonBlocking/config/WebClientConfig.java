package com.nonBlocking.config;

import com.nonBlocking.common.ThrowingConsumer;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    private static final int READ_TIME_OUT_HANDLER = 5;     // millis
    private static final int WRITE_TIME_OUT_HANDLER = 60;   // sec
    private static final int CONNECT_TIMEOUT_MILLIS = 3000; // sec
    private static final int MAX_IN_MEMORY_SIZE = 1024 * 1024 * 50;   // byte

    @Bean
    public WebClient webClient() {
        // 사용 될 버퍼 메모리를 지정하여 오버플로우를 방지한다.
        // 기본값은 256kb이다.
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                // Http Code설정
                .codecs(clientCodecConfigurer
                        -> clientCodecConfigurer.defaultCodecs()
                .maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();

        exchangeStrategies.messageWriters()
                .stream()
                .filter(LoggingCodecSupport.class::isInstance)
                .forEach(httpMessageWriter ->
                        ((LoggingCodecSupport)httpMessageWriter)
                                .setEnableLoggingRequestDetails(true));

        return WebClient.builder()
                .clientConnector(
                        // Reactor Netty 설정
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                // 보안 설정
                                .secure(ThrowingConsumer.unchecked(
                                        sslContextSpec ->
                                                // SSL 설정
                                                // 모든 인증서 허용
                                                sslContextSpec.sslContext(
                                                        SslContextBuilder.forClient()
                                                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
                                                )
                                        ))
                                // 연결, 읽기, 쓰기의 TimeOut 설정
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                                .doOnConnected(connection ->
                                        connection.addHandlerLast(new ReadTimeoutHandler(READ_TIME_OUT_HANDLER))
                                                    .addHandlerLast(new WriteTimeoutHandler(WRITE_TIME_OUT_HANDLER)))
                        )
                )
                .exchangeStrategies(exchangeStrategies)
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        clientRequest -> {
                            return Mono.just(clientRequest);
                        }
                ))
                .defaultHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.3")
                .build();
    }
}
