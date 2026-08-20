package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.classification;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassification;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.out.AppleCiderClassificationAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleClassificationSystem;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleClassificationView;
import org.craft.mcs.cap.core.cideranalysis.classification.application.port.in.AssessNorthAmericanCiderProfileUseCase;
import org.craft.mcs.cap.core.cideranalysis.classification.application.port.in.ClassifyCiderAppleUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL from AppleProfile classification scenarios to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class AppleCiderClassificationAnalysisAdapter implements AppleCiderClassificationAnalyzer {

  private final @NonNull ClassifyCiderAppleUseCase classifyUseCase;
  private final @NonNull AssessNorthAmericanCiderProfileUseCase northAmericanUseCase;

  @Override
  public @NonNull AppleVarietyClassification classify(
      @NonNull AppleVarietyAnalysisProfile profile,
      @NonNull AppleVarietyClassificationSystem system
  ) {
    final var result = classifyUseCase.classify(
        characteristics(profile),
        CiderAppleClassificationSystem.valueOf(system.name())
    );
    return new AppleVarietyClassification(
        profile.varietyId(),
        profile.name(),
        result.profileCode(),
        result.classified(),
        result.appleClass(),
        AppleVarietyClassification.Confidence.valueOf(result.confidence().name()),
        result.borderlineEvidence().stream()
            .map(AppleCiderClassificationAnalysisAdapter::toAppleEvidence)
            .toList()
    );
  }

  @Override
  public @NonNull AppleVarietyNorthAmericanAssessmentView assessNorthAmerican(
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    final var result = northAmericanUseCase.assess(characteristics(profile));
    final var sugar = result.sugar();
    final var acidity = result.acidity();
    final var tannin = result.tannin();
    return new AppleVarietyNorthAmericanAssessmentView(
        sugar == null
            ? null
            : AppleVarietyNorthAmericanAssessmentView.SugarLevel.valueOf(sugar.name()),
        acidity == null
            ? null
            : AppleVarietyNorthAmericanAssessmentView.AcidityLevel.valueOf(acidity.name()),
        tannin == null
            ? null
            : AppleVarietyNorthAmericanAssessmentView.TanninLevel.valueOf(tannin.name())
    );
  }

  private static @NonNull CiderAppleCharacteristics characteristics(
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    return new CiderAppleCharacteristics(
        profile.specificGravity(),
        profile.titratableAcidity(),
        profile.tannin()
    );
  }

  private static AppleVarietyClassification.@NonNull BorderlineEvidence toAppleEvidence(
      CiderAppleClassificationView.@NonNull BorderlineEvidence evidence
  ) {
    return new AppleVarietyClassification.BorderlineEvidence(
        AppleVarietyClassification.Characteristic.valueOf(evidence.characteristic().name()),
        AppleVarietyClassification.Reason.valueOf(evidence.reason().name())
    );
  }
}
