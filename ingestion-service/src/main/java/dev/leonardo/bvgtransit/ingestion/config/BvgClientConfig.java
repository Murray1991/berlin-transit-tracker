package dev.leonardo.bvgtransit.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class BvgClientConfig {

    @Bean
    WebClient bvgWebClient(BvgApiProperties properties) {
        String bvgBaseURL =
                UriComponentsBuilder.newInstance()
                        .scheme(properties.scheme())
                        .host(properties.host())
                        .port(properties.port())
                        .build()
                        .toUriString();
        return WebClient.builder().baseUrl(bvgBaseURL).build();
    }

    @ConfigurationProperties(prefix = "bvg.api")
    public record BvgApiProperties(String scheme, String host, int port) {}
}
