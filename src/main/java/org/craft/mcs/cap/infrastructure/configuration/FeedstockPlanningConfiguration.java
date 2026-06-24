package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase.FeedstockPlanningMeasurementFreshnessPolicy;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(FeedstockPlanningProperties.class)
class FeedstockPlanningConfiguration {

  @Bean
  FeedstockPlanningMeasurementFreshnessPolicy feedstockPlanningMeasurementFreshnessPolicy(
      @NonNull FeedstockPlanningProperties properties
  ) {
    return new FeedstockPlanningMeasurementFreshnessPolicy(
        properties.measurementFreshness().staleAfter()
    );
  }
}
