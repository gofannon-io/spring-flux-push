package xyz.gofannon.springfluxpush;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloRestController {

    @GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sayHello() {
        return "Hello World!";
    }

    @GetMapping(value = "/hello/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public Message sayHelloXml() {
        return new Message("Hello World!");
    }
}
