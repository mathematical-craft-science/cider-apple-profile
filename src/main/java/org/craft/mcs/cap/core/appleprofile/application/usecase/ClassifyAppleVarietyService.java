package org.craft.mcs.cap.core.appleprofile.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassification;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.cideranalysis.classification.result.ClassificationResult;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ChemicalProfileClassifier;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class ClassifyAppleVarietyService
    implements ClassifyAppleVarietyUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileClassifier classifier;
  private final @NonNull AppleVarietyClassificationStrategyRegistry strategyRegistry;

  @Override
  public @NonNull AppleVarietyClassification classify(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyClassificationSystem system
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(system, "classification system cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new IllegalArgumentException("apple variety not found: " + varietyId.value()));

    final var result = classifyUnchecked(variety.chemicalProfile(), strategyRegistry.get(system));

    return new AppleVarietyClassification(
        variety.varietyId(),
        variety.name(),
        result.profileCode(),
        result.classified(),
        result.appleClass() == null ? null : result.appleClass().name(), // NOSONAR
        result.confidence(),
        result.borderline(),
        result.borderlineEvidence()
    );
  }

  private @NonNull ClassificationResult<?> classifyUnchecked(
      @NonNull ChemicalProfile profile,
      @NonNull ClassificationStrategy<?> strategy
  ) {
    return classifier.classify(profile, strategy);
  }
}
