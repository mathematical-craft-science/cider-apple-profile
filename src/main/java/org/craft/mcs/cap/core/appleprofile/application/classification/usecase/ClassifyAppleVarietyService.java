package org.craft.mcs.cap.core.appleprofile.application.classification.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.AppleVarietyClassification;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.result.ClassificationResult;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ChemicalProfileClassifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for classifying an apple variety by one configured cider classification
 * system.
 *
 * <p>This use case loads the catalog aggregate, resolves the selected strategy through
 * {@link AppleVarietyClassificationStrategyRegistry}, delegates the actual domain classification to
 * {@link ChemicalProfileClassifier}, and maps the result to an application read model. It is the
 * boundary where REST/API system names become domain strategy selection.
 */
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
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));

    final var result = classifyResolved(variety.chemicalProfile(), system);

    return new AppleVarietyClassification(
        variety.varietyId(),
        variety.name(),
        result.profileCode(),
        result.classified(),
        appleClassName(result),
        result.confidence(),
        result.borderline(),
        result.borderlineEvidence()
    );
  }

  private <C extends Enum<C>> @NonNull ClassificationResult<C> classifyResolved(
      @NonNull ChemicalProfile profile,
      @NonNull AppleVarietyClassificationSystem system
  ) {
    final ClassificationStrategy<C> strategy = strategyRegistry.get(system);
    return classifier.classify(profile, strategy);
  }

  private static <C extends Enum<C>> @Nullable String appleClassName(
      @NonNull ClassificationResult<C> result
  ) {
    final var appleClass = result.appleClass();
    return appleClass == null ? null : appleClass.name();
  }
}
