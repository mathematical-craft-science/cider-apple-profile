package org.craft.mcs.cap.shared.domain.classification.engine;

import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.result.ClassificationResult;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Service
public class ChemicalProfileClassifier {

  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ClassificationConfidencePolicy confidencePolicy;

  public ChemicalProfileClassifier(
      @NonNull ChemicalProfileAxisExtractor axisExtractor,
      @NonNull ClassificationConfidencePolicy confidencePolicy
  ) {
    this.axisExtractor = requireNonNull(axisExtractor, "chemical profile axis extractor cannot be null");
    this.confidencePolicy = requireNonNull(confidencePolicy, "classification confidence policy cannot be null");
  }

  public <C extends Enum<C>> @NonNull ClassificationResult<C> classify(
      @NonNull ChemicalProfile profile,
      @NonNull ClassificationStrategy<C> strategy
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    requireNonNull(strategy, "classification strategy cannot be null");

    final var axes = axisExtractor.extract(profile);
    final var classificationProfile = strategy.profile();

    return classificationProfile.rules()
        .stream()
        .filter(rule -> rule.matches(axes))
        .findFirst()
        .map(rule -> {
          final var assessment = confidencePolicy.assess(rule, axes);

          return ClassificationResult.classified(
              classificationProfile.code(),
              rule.appleClass(),
              assessment.confidence(),
              assessment.borderline(),
              assessment.evidence(),
              axes
          );
        })
        .orElseGet(() -> ClassificationResult.unclassified(
            classificationProfile.code(),
            axes
        ));
  }
}
