package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisReadinessConfiguration {

  @Bean
  FermentationStartReadinessAssessor fermentationStartReadinessAssessor() {
    return new FermentationStartReadinessAssessor();
  }
}
