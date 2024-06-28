package xyz.gofannon.springfluxpush;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.StringWriter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class WeatherRestController2 {

    private static final Logger logger = LoggerFactory.getLogger(WeatherRestController2.class);

    private Flux<ServerSentEvent<String>> notificationFlux;

    private final AtomicInteger counter = new AtomicInteger(0);

    private Marshaller marshaller;

    @PostConstruct
    public void initialize() throws JAXBException {
        this.notificationFlux = Flux.push(this::generateNotifications);

        JAXBContext context = JAXBContext.newInstance(WeatherBulletin.class);
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }


    @GetMapping(value = "/weather2", produces = MediaType.APPLICATION_XML_VALUE)
    public Flux<ServerSentEvent<String>> subscribe() {
        return notificationFlux;
    }


    private ServerSentEvent<String> generateNotification() {
        return ServerSentEvent.<String>builder()
                .data(generateWeatherBulletin())
                .build();
    }


    private String generateWeatherBulletin() {
        var bulletin = new WeatherBulletin(counter.incrementAndGet(), "Sunny");
        try {
            StringWriter writer = new StringWriter();
            marshaller.marshal(bulletin, writer);
            return writer.toString();
        } catch (JAXBException ex) {
            return "<error>";
        }
    }


    private void generateNotifications(FluxSink<ServerSentEvent<String>> sink) {
        Flux.interval(Duration.ofSeconds(3)) // Generate simple notifications every 2 seconds.
                .map(it -> generateNotification())
                .doOnNext(serverSentEvent -> {
                    sink.next(serverSentEvent); // Sending notifications to the global Flux via its FluxSink
                    logger.info("Sent for {}", serverSentEvent.data());
                })
                .doFinally(signalType -> logger.info("Notification flux closed")) // Logging the closure of our generator
                .takeWhile(notification -> !sink.isCancelled()) // We generate messages until the global Flux is closed
                .subscribe();
    }
}
