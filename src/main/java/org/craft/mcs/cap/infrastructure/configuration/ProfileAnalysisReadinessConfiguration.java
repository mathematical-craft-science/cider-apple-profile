package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisReadinessConfiguration {

  @Bean
  FermentationStartReadinessAssessor fermentationStartReadinessAssessor() {
    return new FermentationStartReadinessAssessor();
  }
}
