package org.craft.mcs.cap.core.cideranalysis.classification.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleClassificationSystem;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleClassificationView;
import org.craft.mcs.cap.core.cideranalysis.classification.application.port.in.ClassifyCiderAppleUseCase;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.CiderAppleClassifier;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.french.FrenchClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton.LongAshton4ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton.LongAshton7ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.spanish.SpanishClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.BorderlineEvidence;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.ClassificationResult;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Application boundary for classifying cider apples with a selected named system. */
@Component
@RequiredArgsConstructor
public final class ClassifyCiderAppleService implements ClassifyCiderAppleUseCase {

  private final @NonNull CiderAppleClassifier classifier;

  @Override
  public @NonNull CiderAppleClassificationView classify(
      @NonNull CiderAppleCharacteristics characteristics,
      @NonNull CiderAppleClassificationSystem system
  ) {
    requireNonNull(characteristics, "cider-apple characteristics cannot be null");
    final var classificationSystem = requireNonNull(system, "classification system cannot be null");
    final var input = input(characteristics);
    return switch (classificationSystem) {
      case FRENCH -> toView(classifier.classify(
          input,
          new FrenchClassificationStrategy(BuiltInCiderClassificationThresholds.french())
      ));
      case SPANISH -> toView(classifier.classify(
          input,
          new SpanishClassificationStrategy(BuiltInCiderClassificationThresholds.spanish())
      ));
      case LONG_ASHTON_4 -> toView(classifier.classify(
          input,
          new LongAshton4ClassificationStrategy(BuiltInCiderClassificationThresholds.longAshton4())
      ));
      case LONG_ASHTON_7 -> toView(classifier.classify(
          input,
          new LongAshton7ClassificationStrategy(BuiltInCiderClassificationThresholds.longAshton7())
      ));
    };
  }

  private static @NonNull CiderAppleClassificationInput input(
      @NonNull CiderAppleCharacteristics characteristics
  ) {
    return new CiderAppleClassificationInput(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin()
    );
  }

  private static <C extends Enum<C>> @NonNull CiderAppleClassificationView toView(
      @NonNull ClassificationResult<C> result
  ) {
    final var appleClass = result.appleClass();
    return new CiderAppleClassificationView(
        result.profileCode(),
        result.classified(),
        appleClass == null ? null : appleClass.name(),
        CiderAppleClassificationView.Confidence.valueOf(result.confidence().name()),
        result.borderlineEvidence().stream()
            .map(ClassifyCiderAppleService::toView)
            .toList()
    );
  }

  private static CiderAppleClassificationView.@NonNull BorderlineEvidence toView(
      @NonNull BorderlineEvidence evidence
  ) {
    return new CiderAppleClassificationView.BorderlineEvidence(
        CiderAppleClassificationView.Characteristic.valueOf(evidence.characteristic().name()),
        CiderAppleClassificationView.BorderlineReason.valueOf(evidence.reason().name())
    );
  }
}
