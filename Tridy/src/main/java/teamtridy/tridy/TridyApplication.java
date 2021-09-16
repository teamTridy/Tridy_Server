package teamtridy.tridy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TridyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TridyApplication.class, args);
    }

}
