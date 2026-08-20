package org.craft.mcs.cap.core.cideranalysis.classification.domain.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.BorderlineEvidence;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.BorderlineReason;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.ClassificationAssessment;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.result.ClassificationConfidence;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationRule;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isAtMost;


/** Assesses confidence from typed input shape and proximity to classification thresholds. */
@Service
public class ClassificationConfidencePolicy {

  private static final BigDecimal DEFAULT_BOUNDARY_MARGIN_RATIO = new BigDecimal("0.05");

  public <C extends Enum<C>> @NonNull ClassificationAssessment assess(
      @NonNull ClassificationRule<C> matchedRule,
      @NonNull CiderAppleClassificationInput input,
      @NonNull ClassificationIntervalPolicy intervalPolicy
  ) {
    requireNonNull(matchedRule, "matched classification rule cannot be null");
    requireNonNull(input, "cider apple classification input cannot be null");
    requireNonNull(intervalPolicy, "classification interval policy cannot be null");

    final var evidence = new ArrayList<BorderlineEvidence>();
    for (final var entry : matchedRule.ranges().entrySet()) {
      final var characteristic = entry.getKey();
      final var range = entry.getValue();
      if (!input.has(characteristic)) {
        evidence.add(new BorderlineEvidence(characteristic, BorderlineReason.MISSING_AXIS));
        continue;
      }
      detectCloseToBoundary(
          characteristic,
          input.representativeValue(characteristic, intervalPolicy),
          range,
          evidence
      );
      input.interval(characteristic)
          .ifPresent(interval -> detectRangeOverlap(characteristic, interval, range, evidence));
    }

    final var confidence = confidenceFrom(evidence, matchedRule, input);
    return new ClassificationAssessment(
        confidence,
        evidence
    );
  }

  private static void detectCloseToBoundary(
      @NonNull ClassificationCharacteristic characteristic,
      @NonNull BigDecimal value,
      @NonNull ClassificationValueRange range,
      @NonNull List<@NonNull BorderlineEvidence> evidence
  ) {
    if (isCloseTo(value, range.lowerBoundary()) || isCloseTo(value, range.upperBoundary())) {
      evidence.add(new BorderlineEvidence(characteristic, BorderlineReason.CLOSE_TO_BOUNDARY));
    }
  }

  private static void detectRangeOverlap(
      @NonNull ClassificationCharacteristic characteristic,
      @NonNull ClassificationInterval interval,
      @NonNull ClassificationValueRange range,
      @NonNull List<@NonNull BorderlineEvidence> evidence
  ) {
    if (!range.contains(interval.lower()) || !range.contains(interval.upper())) {
      evidence.add(new BorderlineEvidence(characteristic, BorderlineReason.RANGE_OVERLAPS_BOUNDARY));
    }
  }

  private static boolean isCloseTo(
      @NonNull BigDecimal value,
      @Nullable BigDecimal boundary
  ) {
    if (boundary == null || boundary.signum() == 0) {
      return false;
    }
    final var margin = boundary.abs().multiply(DEFAULT_BOUNDARY_MARGIN_RATIO);
    return isAtMost(value.subtract(boundary).abs(), margin);
  }

  private static <C extends Enum<C>> @NonNull ClassificationConfidence confidenceFrom(
      @NonNull List<@NonNull BorderlineEvidence> evidence,
      @NonNull ClassificationRule<C> matchedRule,
      @NonNull CiderAppleClassificationInput input
  ) {
    if (!evidence.isEmpty()) {
      return ClassificationConfidence.LOW;
    }
    final var hasOnlyPointValues = matchedRule.ranges()
        .keySet()
        .stream()
        .allMatch(input::isPoint);
    return hasOnlyPointValues ? ClassificationConfidence.HIGH : ClassificationConfidence.MEDIUM;
  }
}
