package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseClient {

    protected static final String SERVER_URL = "http://localhost:9090";

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected <T> ResponseEntity<T> get(String url,
                                        Map<String, Object> params,
                                        Class<T> responseType,
                                        String headerName,
                                        Long userId) {
        return exchange(url, HttpMethod.GET, null, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> get(String url,
                                        Map<String, Object> params,
                                        ParameterizedTypeReference<T> responseType,
                                        String headerName,
                                        Long userId) {
        return exchange(url, HttpMethod.GET, null, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> post(String url,
                                         Object body,
                                         Map<String, Object> params,
                                         Class<T> responseType,
                                         String headerName,
                                         Long userId) {
        return exchange(url, HttpMethod.POST, body, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> post(String url,
                                         Object body,
                                         Map<String, Object> params,
                                         ParameterizedTypeReference<T> responseType,
                                         String headerName,
                                         Long userId) {
        return exchange(url, HttpMethod.POST, body, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> patch(String url,
                                          Object body,
                                          Map<String, Object> params,
                                          Class<T> responseType,
                                          String headerName,
                                          Long userId) {
        return exchange(url, HttpMethod.PATCH, body, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> patch(String url,
                                          Object body,
                                          Map<String, Object> params,
                                          ParameterizedTypeReference<T> responseType,
                                          String headerName,
                                          Long userId) {
        return exchange(url, HttpMethod.PATCH, body, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> delete(String url,
                                           Map<String, Object> params,
                                           Class<T> responseType,
                                           String headerName,
                                           Long userId) {
        return exchange(url, HttpMethod.DELETE, null, params, responseType, headerName, userId);
    }

    protected <T> ResponseEntity<T> exchange(String url,
                                             HttpMethod method,
                                             Object body,
                                             Map<String, Object> params,
                                             Class<T> responseType,
                                             String headerName,
                                             Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (headerName != null && userId != null) {
            headers.set(headerName, userId.toString());
        }
        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        try {
            log.debug("CLIENT: {} {} userId={} params={}", method, url, userId, params);
            ResponseEntity<T> response = restTemplate.exchange(SERVER_URL + url, method, request, responseType, params);
            log.info("CLIENT: Успех {} {} userId={} статус={}", method, url, userId, response.getStatusCode());
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("CLIENT ERROR {} {} userId={}: {}", method, url, userId, e.getResponseBodyAsString());
            T errorBody = null;
            try { errorBody = objectMapper.readValue(e.getResponseBodyAsString(), responseType); } catch (Exception ignored) {}
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        } catch (Exception e) {
            log.error("CLIENT ERROR {} {} userId={}: {}", method, url, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    protected <T> ResponseEntity<T> exchange(String url,
                                             HttpMethod method,
                                             Object body,
                                             Map<String, Object> params,
                                             ParameterizedTypeReference<T> responseType,
                                             String headerName,
                                             Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (headerName != null && userId != null) {
            headers.set(headerName, userId.toString());
        }
        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        try {
            log.debug("CLIENT: {} {} userId={} params={}", method, url, userId, params);
            ResponseEntity<T> response = restTemplate.exchange(SERVER_URL + url, method, request, responseType, params);
            log.info("CLIENT: Успех {} {} userId={} статус={}", method, url, userId, response.getStatusCode());
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("CLIENT ERROR {} {} userId={}: {}", method, url, userId, e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            log.error("CLIENT ERROR {} {} userId={}: {}", method, url, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
