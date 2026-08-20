package org.craft.mcs.cap.infrastructure.configuration;


import org.craft.mcs.cap.core.cideranalysis.abv.domain.AbvPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class CiderAnalysisAbvConfiguration {

  @Bean
  FinalGravityPredictionStrategy finalGravityPredictionStrategy() {
    return FixedFinalGravityPredictionStrategy.dryCiderDefault();
  }

  @Bean
  AbvPredictionStrategy abvPredictionStrategy() {
    return new StandardGravityDropAbvPredictionStrategy();
  }

  @Bean
  PotentialAbvPredictor potentialAbvPredictor(
      FinalGravityPredictionStrategy finalGravityPredictionStrategy,
      AbvPredictionStrategy abvPredictionStrategy
  ) {
    return new PotentialAbvPredictor(finalGravityPredictionStrategy, abvPredictionStrategy);
  }
}
