package com.nonBlocking.controller;

import com.nonBlocking.common.ResponseData;
import com.nonBlocking.config.RestTemplateConfig;
import com.nonBlocking.config.WebClientConfig;
import com.nonBlocking.service.RestTemplateService;
import com.nonBlocking.service.SocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Controller
@RequiredArgsConstructor
public class APIController {
    private Logger log = LoggerFactory.getLogger(APIController.class);
    ApplicationContext responseDataAC = new AnnotationConfigApplicationContext(ResponseData.class);
    ResponseData responseData = responseDataAC.getBean(ResponseData.class);
    private final SocketService socketService;
    private final RestTemplateService restTemplateService;
    String url = "http://localhost:8080/apiTest";
    WebClient webClient = WebClient.create("http://localhost:8080");

    /*
    * Blocking 통신 구현을 위한 RestTemplate 선언
    * HTTP Server와의 통신을 단순화하고 RESTful 원칙을 지킨다.
    */
    private final RestTemplateConfig restTemplateConfig;
    RestTemplate restTemplate = null;

    @Autowired
    private WebClientConfig webClientConfig;

    // 아래 API는 다른 Server에 추가하여 API Test 통신을 받아준다.
    @PostMapping("/apiTest")
    // 평소 Server 구현 시 ResponseEntity에 ResponseData라는 객체를 담아 처리하기에 이를 그대로 가져왔다.
    public ResponseEntity<ResponseData> apiTest(@RequestBody HashMap<String, Object> paramMap) throws InterruptedException {
        log.trace("Call apiTest.");

        // ResponseServer에서는 전달받은 Data에서 errorMsg를 추출해서 로그로 뿌려주면서 호출 Server를 확인해주고
        String errorMsg = paramMap.get("errorMsg").toString();
        log.info("errorMsg : {}", errorMsg);

        // 응답으로 전달할 메시지를 저장한뒤
        responseData.setErrorMsg("http://localhost:8080/apiTest - Call apiTest.");

        // 3초의 간격을 주었다.
        Thread.sleep(3000);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/blocking")
    public ResponseEntity<ResponseData> Blocking() {
        log.trace("Call blocking.");
        restTemplate = restTemplateConfig.restTemplate(
                restTemplateConfig.factory(
                        restTemplateConfig.httpClient()
                )
        );

        // 시간 측정을 위해 선언
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String body = "";
        ResponseEntity responseEntity = null;
        for(int i = 0; i < 3; i++) {
            // Response Server API 호출
            body = "http://localhost:18080/blocking - Call blocking.";
            responseEntity = restTemplateService.write(restTemplate, url, HttpMethod.POST, body);
            body = responseEntity.getBody().toString();
            log.info("body : {}", body);
        }

        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return responseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/nonBlocking/webcline01")
    public Mono<ResponseEntity<ResponseData>> NonBlockingWebClient01() {
        log.trace("Call nonBlocking webcline01.");

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        responseData.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline01.");

        Mono<ResponseData> responseEntityMono = null;
        for(int i = 0; i < 3; i++) {
            responseEntityMono = webClientConfig.webClient()
                    .mutate()
                    .baseUrl("http://localhost:8080")
                    .build()
                    .post()
                    .uri("/apiTest")
                    .bodyValue(responseData)
                    .retrieve()
                    .bodyToMono(ResponseData.class);

            // block()메소드를 이용하여 결과 값을 가져오는데 여기서 Block이 걸린다.
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

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Mono<ResponseData> responseEntityMono = null;
        for(int i = 0; i < 3; i++) {
            responseData.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline02(" + i + ")");
            webClientConfig.webClient()
                    .mutate()
                    .baseUrl("http://localhost:8080")
                    .build()
                    .post()
                    .uri("/apiTest")
                    .bodyValue(responseData)
                    .retrieve()
                    .bodyToMono(ResponseData.class)
                    // 호출 순서를 확인하기 위한 응답 값 출력
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

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Mono<ResponseEntity<ResponseData>> responseEntityMono = null;
        for(int i = 0; i < 3; i++) {
            responseData.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline03(" + i + ")");
            log.info("responseData.getErrorMsg() : {}", responseData.getErrorMsg());
            responseEntityMono = webClientConfig.webClient()
                    .mutate()
                    .baseUrl("http://localhost:8080")
                    .build()
                    .post()
                    .uri("/apiTest")
                    .bodyValue(responseData)
                    .retrieve()
                    .toEntity(ResponseData.class);
        }

        /*
        * 비즈니스 로직 수행 ...
        * */
        responseEntityMono.subscribe(response ->
                log.info("body : {}", response));
        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.OK).body(responseData));
    }

    @PostMapping("/nonBlocking/webcline04")
    public Mono<ResponseEntity<ResponseData>> NonBlockingWebClient04() {
        log.trace("Call nonBlocking webcline04.");


        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Flux.range(0,3)
                .flatMap(t ->
                        getWebClient04(t.toString()))
                .subscribe(response -> log.info("body : {}" ,response.getBody()));
        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.OK).body(responseData));
    }

    public Mono<ResponseData> getWebClient04(String i) {
        ResponseData responseData01 = new ResponseData();
        return webClient.post()
                .uri("/apiTest")
                //.bodyValue(responseData01.setErrorMsg("http://localhost:18080/nonBlocking - Call nonBlocking webcline04(" + i + ")"))
                .retrieve()
                .bodyToMono(ResponseData.class);
    }

    @PostMapping("/blockingSocket")
    public ResponseEntity<ResponseData> BlockingSocket() {
        log.trace("Call blockingSocket.");

        // 시간 측정을 위해 선언
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ResponseEntity responseEntity = null;
        responseData.setErrorMsg("http://localhost:18080 - Call blockingSocket.");

        //socketService.BlockingSocketInit("localhost", 28080);
        for (int i = 0; i < 3; i++) {
            responseData = socketService.BlockingSocket(responseData.toString());
        }
        //socketService.BlockingSocketRead();

        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return responseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/nonBlockingSocket")
    public ResponseEntity<ResponseData> NonBlockingSocket() {
        log.info("Call nonBlockingSocket API.");

        // 시간 측정을 위해 선언
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ResponseEntity responseEntity = null;
        responseData.setErrorMsg("http://localhost:18080 - Call nonBlockingSocket.");

        socketService.NonBlockingSocketInit("localhost", 28080);
        SocketService.Receive receive = new SocketService.Receive();
        new Thread(receive).start();
        for (int i = 0; i < 3; i++) {
            socketService.NonBlockingSocketWrite("http://localhost:18080 - Call nonBlockingSocket.");
        }
        stopWatch.stop();
        log.info("Total Second : {}", stopWatch.getTotalTimeSeconds());

        return responseEntity.status(HttpStatus.OK).body(responseData);
    }
}
