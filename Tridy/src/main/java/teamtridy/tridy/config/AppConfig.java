package teamtridy.tridy.config;

import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@EnableJpaAuditing
@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .requestFactory(
                        () -> new BufferingClientHttpRequestFactory(
                                new SimpleClientHttpRequestFactory()))
                .setConnectTimeout(Duration.ofMillis(5000)) // connection-timeout
                .setReadTimeout(Duration.ofMillis(5000)) // read-timeout
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();
    }

    // "" can't convert to Items error
    //https://stackoverflow.com/questions/68015096/how-to-handle-invalidformatexception-for-empty-values-on-enumerations
    //https://cowtowncoder.medium.com/jackson-2-12-most-wanted-4-5-cbc91c00bcd2
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> {
            objectMapper.coercionConfigFor(LogicalType.POJO)
                    .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        });
    }

}
