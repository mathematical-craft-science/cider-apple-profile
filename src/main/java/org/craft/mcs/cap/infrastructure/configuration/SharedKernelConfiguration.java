package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class SharedKernelConfiguration {

  @Bean
  ChemicalProfileAxisExtractor chemicalProfileAxisExtractor() {
    return new ChemicalProfileAxisExtractor();
  }

  @Bean
  ChemicalProfileCapabilityAssessor chemicalProfileCapabilityAssessor() {
    return new ChemicalProfileCapabilityAssessor();
  }
}
