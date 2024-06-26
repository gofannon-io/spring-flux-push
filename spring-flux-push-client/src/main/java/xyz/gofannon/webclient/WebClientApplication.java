package xyz.gofannon.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public class WebClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(WebClientApplication.class);

    public static void main(String[] args) throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8080");
        ParameterizedTypeReference<ServerSentEvent<String>> type
                = new ParameterizedTypeReference<ServerSentEvent<String>>() {};

        Flux<ServerSentEvent<String>> eventStream = client.get()
                .uri("/weather2")
                .retrieve()
                .bodyToFlux(type);

        eventStream.subscribe(
                WebClientApplication::parseEvent,
                error -> logger.error("Error receiving SSE: {}", error),
                () -> logger.info("Completed!!!"));

        while(true) {
            Thread.sleep(10000);
        }
    }

    private static void parseEvent(ServerSentEvent<String> content) {
        logger.info("Time: {} - content: {}",
                LocalDateTime.now(), content.data());
    }

}
