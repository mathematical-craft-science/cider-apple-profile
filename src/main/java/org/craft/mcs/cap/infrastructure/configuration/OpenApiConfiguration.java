package org.craft.mcs.cap.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfiguration {

  @Bean
  public @NonNull OpenAPI ciderAppleProfileOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Cider Apple Profile API")
            .version("0.0.1")
            .description("API for cider apple profiles, feedstock batches, chemical profile plotting and fermentation predictions."));
  }
}
