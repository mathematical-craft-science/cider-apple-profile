package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase.FeedstockCurrentMeasurementFreshnessPolicy;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(FeedstockCurrentProfileProperties.class)
class FeedstockCurrentProfileConfiguration {

  @Bean
  FeedstockCurrentMeasurementFreshnessPolicy feedstockCurrentMeasurementFreshnessPolicy(
      @NonNull FeedstockCurrentProfileProperties properties
  ) {
    return new FeedstockCurrentMeasurementFreshnessPolicy(
        properties.measurementFreshness().staleAfter()
    );
  }
}
