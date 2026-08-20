package org.craft.mcs.cap.core.cideranalysis.classification.domain.engine;

import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.ClassificationResult;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Applies one cider-apple classification strategy to its typed scenario input. */
@Service
public final class CiderAppleClassifier {

  private final @NonNull ClassificationConfidencePolicy confidencePolicy;

  public CiderAppleClassifier(@NonNull ClassificationConfidencePolicy confidencePolicy) {
    this.confidencePolicy = requireNonNull(
        confidencePolicy,
        "classification confidence policy cannot be null"
    );
  }

  public <C extends Enum<C>> @NonNull ClassificationResult<C> classify(
      @NonNull CiderAppleClassificationInput input,
      @NonNull ClassificationStrategy<C> strategy
  ) {
    requireNonNull(input, "cider apple classification input cannot be null");
    requireNonNull(strategy, "classification strategy cannot be null");
    if (!input.missing(strategy.requiredCharacteristics()).isEmpty()) {
      return ClassificationResult.unclassified(strategy.code());
    }

    final var profile = strategy.profile();
    final var intervalPolicy = strategy.intervalPolicy();
    return profile.rules()
        .stream()
        .filter(rule -> rule.matches(input, intervalPolicy))
        .findFirst()
        .map(rule -> {
          final var assessment = confidencePolicy.assess(rule, input, intervalPolicy);
          return ClassificationResult.classified(
              profile.code(),
              rule.appleClass(),
              assessment.confidence(),
              assessment.evidence()
          );
        })
        .orElseGet(() -> ClassificationResult.unclassified(profile.code()));
  }
}
