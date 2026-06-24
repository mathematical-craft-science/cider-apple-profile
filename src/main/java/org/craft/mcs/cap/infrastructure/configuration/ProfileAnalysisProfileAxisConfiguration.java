package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisProfileAxisConfiguration {

  @Bean
  ChemicalProfileCapabilityAssessor chemicalProfileCapabilityAssessor() {
    return new ChemicalProfileCapabilityAssessor();
  }
}
