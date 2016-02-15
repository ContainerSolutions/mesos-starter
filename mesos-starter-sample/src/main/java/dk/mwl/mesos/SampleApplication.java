package dk.mwl.mesos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SampleApplication {
    @RequestMapping("/")
    public String index() {
        return "Hello";
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
