package xyz.gofannon.springfluxpush;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class WeatherRestController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherRestController.class);

    private Flux<ServerSentEvent<WeatherBulletin>> notificationFlux;

    private final AtomicInteger counter = new AtomicInteger(0);


    @PostConstruct
    public void initialize() {
        this.notificationFlux = Flux.push(this::generateNotifications);
    }


    @GetMapping(value = "/weather", produces = MediaType.APPLICATION_XML_VALUE)
    public Flux<ServerSentEvent<Message>> subscribe() {
        return keepAlive(Duration.ofSeconds(5), notificationFlux);
    }

    private <T> Flux keepAlive(Duration duration, Flux<T> data) {
        Flux<ServerSentEvent<Message>> heartBeat = Flux.interval(duration)
                .map(
                        e -> ServerSentEvent.<Message>builder()
                                .comment("keep alive for: ")
                                .build())
                .doFinally(signalType -> logger.info("Heartbeat closed"));
        return Flux.merge(heartBeat, data);
    }

    private ServerSentEvent<WeatherBulletin> generateNotification() {
        return ServerSentEvent.<WeatherBulletin>builder()
                .data(generateWeatherBulletin())
                .build();
    }


    private WeatherBulletin generateWeatherBulletin() {
        return new WeatherBulletin(counter.incrementAndGet(), "Sunny");
    }


    private void generateNotifications(FluxSink<ServerSentEvent<WeatherBulletin>> sink) {
        Flux.interval(Duration.ofSeconds(3)) // Generate simple notifications every 2 seconds.
                .map(it -> generateNotification())
                .doOnNext(serverSentEvent -> {
                    sink.next(serverSentEvent); // Sending notifications to the global Flux via its FluxSink
                    logger.info("Sent for {}", serverSentEvent.data().getId());
                })
                .doFinally(signalType -> logger.info("Notification flux closed")) // Logging the closure of our generator
                .takeWhile(notification -> !sink.isCancelled()) // We generate messages until the global Flux is closed
                .subscribe();
    }
}
