package com.nonBlocking.service;

import com.nonBlocking.common.ResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RestTemplateService {
    private final ResponseData responseData;
    ResponseEntity responseEntity;
    public ResponseEntity write(RestTemplate restTemplate, String url, HttpMethod httpMethod, String body) {
        // Set Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        responseData.setErrorMsg(body);
        HttpEntity<ResponseData> responseDataHttpEntity = new HttpEntity<>(responseData, headers);

        responseEntity = restTemplate.exchange(url, httpMethod, responseDataHttpEntity, ResponseData.class);

        return responseEntity;
    }
}
