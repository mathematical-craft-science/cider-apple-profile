package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
import org.craft.mcs.cap.core.profileanalysis.graph.application.service.JolicoeurSugarAcidityGraphEvaluator;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ProfileAnalysisCompatibilityConfiguration {

  @Bean
  ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter(
      ChemicalProfileAxisExtractor axisExtractor,
      JolicoeurSugarAcidityGraphEvaluator graphEvaluator,
      NorthAmericanProfileCalculator northAmericanProfileCalculator,
      ChemicalProfileCapabilityAssessor capabilityAssessor
  ) {
    return new ChemicalProfileAssessmentAdapter(
        axisExtractor,
        graphEvaluator,
        northAmericanProfileCalculator,
        capabilityAssessor
    );
  }

  @Bean
  ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher(
      ChemicalProfileCapabilityAssessor capabilityAssessor
  ) {
    return new ChemicalProfileAssessmentMatcher(capabilityAssessor);
  }
}
