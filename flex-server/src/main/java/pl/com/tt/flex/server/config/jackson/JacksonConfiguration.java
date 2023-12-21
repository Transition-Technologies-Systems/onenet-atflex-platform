package pl.com.tt.flex.server.config.jackson;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;
import pl.com.tt.flex.server.config.jackson.serializers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Configuration
public class JacksonConfiguration {

    /**
     * Support for Java date and time API.
     *
     * @return the corresponding Jackson module.
     */
    @Bean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public Jdk8Module jdk8TimeModule() {
        return new Jdk8Module();
    }

    /*
     * Support for Hibernate types in Jackson.
     */
    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    /*
     * Module for serialization/deserialization of RFC7807 Problem.
     */
    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule();
    }

    /*
     * Module for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
            .defaultViewInclusion(true)
            // Pakiety powinny byc ograniczone tylko do modelu, po przeniesieniu wszystch DTO do modelu
            // naley poprawic konfiguracje
            .serializerByType(String.class, new ViewWithAuthorityStringSerializer(Set.of("pl.com.tt.flex")))
            .serializerByType(Number.class, new ViewWithAuthorityNumberSerializer(Set.of("pl.com.tt.flex")))
            .serializerByType(Instant.class, new ViewWithAuthorityInstantSerializer(Set.of("pl.com.tt.flex")))
            .serializerByType(LocalDate.class, new ViewWithAuthorityLocalDateSerializer(Set.of("pl.com.tt.flex")))
            .serializerByType(LocalDateTime.class, new ViewWithAuthorityLocalDateTimeSerializer(Set.of("pl.com.tt.flex")))
            .serializerByType(LocalTime.class, new ViewWithAuthorityLocalTimeSerializer(Set.of("pl.com.tt.flex")));
    }
}
