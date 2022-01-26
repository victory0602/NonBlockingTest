package com.nonBlocking;

import com.nonBlocking.common.ResponseData;
import com.nonBlocking.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Controller
public class APIController {
    private Logger log = LoggerFactory.getLogger(APIController.class);
    ApplicationContext responseDataAC = new AnnotationConfigApplicationContext(ResponseData.class);
    ResponseData responseData = responseDataAC.getBean(ResponseData.class);
    String url = "http://localhost:8080/apiTest";

    /*
    * Blocking 통신 구현을 위한 RestTemplate 선언
    * HTTP Server와의 통신을 단순화하고 RESTful 원칙을 지킨다.
    */
    ApplicationContext restTemplateAC = new AnnotationConfigApplicationContext(RestTemplate.class);
    RestTemplate restTemplate = restTemplateAC.getBean(RestTemplate.class);

    @PostMapping("/blocking")
    public ResponseEntity<ResponseData> Blocking() {
        log.trace("Call blocking.");

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Set Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        responseData.setErrorMsg("http://localhost:18080/blocking - Call blocking.");
        HttpEntity<ResponseData> responseDataHttpEntity = new HttpEntity<>(responseData, headers);
        ResponseEntity responseEntity = null;
        for(int i = 0; i < 3; i++) {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, responseDataHttpEntity, ResponseData.class);
            String body = responseEntity.getBody().toString();
            log.info("body : {}", body);
        }

        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return responseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/nonBlocking/webcline01")
    public Mono<ResponseEntity<ResponseData>> NonBlockingWebClient01() {
        log.trace("Call nonBlocking webcline01.");

        WebClient webClient = WebClient.create("http://localhost:8080");
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        responseData.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline01.");

        Mono<ResponseData> responseEntityMono = null;
        for(int i = 0; i < 3; i++) {
            responseEntityMono = webClient.post()
                    .uri("/apiTest")
                    //.accept(MediaType.APPLICATION_JSON)
                    .bodyValue(responseData)
                    .retrieve()
                    .bodyToMono(ResponseData.class);

            ResponseData body = responseEntityMono.block();
            log.info("body : {}", body.toString());
        }

        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.OK).body(responseData));
    }

    @PostMapping("/nonBlocking/webcline02")
    public Mono<ResponseEntity<ResponseData>> NonBlockingWebClient02() {
        log.trace("Call nonBlocking webcline02.");

        WebClient webClient = WebClient.create("http://localhost:8080");
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        responseData.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline02.");

        Mono<ResponseData> responseEntityMono = null;
        for(int i = 0; i < 3; i++) {
            webClient.post()
                    .uri("/apiTest")
                    //.accept(MediaType.APPLICATION_JSON)
                    .bodyValue(responseData)
                    .retrieve()
                    .bodyToMono(ResponseData.class)
            .subscribe(response ->
                    log.info("body : {}", response));
        }

        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.OK).body(responseData));
    }

    @PostMapping("/nonBlocking/webcline03")
    public Mono<ResponseEntity<ResponseData>> NonBlockingWebClient03() {
        log.trace("Call nonBlocking webcline03.");

        WebClient webClient = WebClient.create("http://localhost:8080");
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        responseData.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline03.");

        Mono<ResponseEntity<ResponseData>> responseEntityMono = null;
        for(int i = 0; i < 3; i++) {
            responseEntityMono = webClient.post()
                    .uri("/apiTest")
                    //.accept(MediaType.APPLICATION_JSON)
                    .bodyValue(responseData)
                    .retrieve()
                    .toEntity(ResponseData.class);
        }

        ResponseEntity body = responseEntityMono.block();
        log.info("body : {}", body.toString());

        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.OK).body(responseData));
    }

    // 아래 API는 다른 Server에 추가하여 API Test 통신을 받아준다.
    @PostMapping("/apiTest")
    public ResponseEntity<ResponseData> apiTest(@RequestBody HashMap<String, Object> paramMap) throws InterruptedException {
        log.trace("Call apiTest.");

        String errorMsg = paramMap.get("errorMsg").toString();
        log.info("errorMsg : {}", errorMsg);
        responseData.setErrorMsg("http://localhost:8080/apiTest - Call apiTest.");

        Thread.sleep(3000);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
