package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisProfileAxisConfiguration {

  @Bean
  ChemicalProfileCapabilityAssessor chemicalProfileCapabilityAssessor() {
    return new ChemicalProfileCapabilityAssessor();
  }
}
