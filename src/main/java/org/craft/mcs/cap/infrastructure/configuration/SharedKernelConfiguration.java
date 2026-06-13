package org.craft.mcs.cap.infrastructure.configuration;


import java.time.Clock;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class SharedKernelConfiguration {

  @Bean
  Clock systemClock() {
    return Clock.systemUTC();
  }

  @Bean
  ChemicalProfileAxisExtractor chemicalProfileAxisExtractor() {
    return new ChemicalProfileAxisExtractor();
  }
}
