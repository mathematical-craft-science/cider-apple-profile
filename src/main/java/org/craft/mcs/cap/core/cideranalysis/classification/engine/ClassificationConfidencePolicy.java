package org.craft.mcs.cap.core.cideranalysis.classification.engine;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.core.cideranalysis.classification.result.BorderlineEvidence;
import org.craft.mcs.cap.core.cideranalysis.classification.result.BorderlineReason;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.core.cideranalysis.classification.result.ClassificationAssessment;
import org.craft.mcs.cap.core.cideranalysis.classification.result.ClassificationConfidence;
import org.craft.mcs.cap.core.cideranalysis.classification.rule.ClassificationRule;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isAtMost;


@Service
public class ClassificationConfidencePolicy {

  private static final BigDecimal DEFAULT_BOUNDARY_MARGIN_RATIO = new BigDecimal("0.05");

  private static void detectCloseToBoundary(
      @NonNull AxisValue axisValue,
      @NonNull AxisRange range,
      @NonNull List<@NonNull BorderlineEvidence> evidence
  ) {
    final var value = axisValue.value();

    if (isCloseTo(value, range.lowerBoundary()) || isCloseTo(value, range.upperBoundary())) {
      evidence.add(new BorderlineEvidence(axisValue.axis(), BorderlineReason.CLOSE_TO_BOUNDARY));
    }
  }

  private static void detectRangeOverlap(
      @NonNull AxisValue axisValue,
      @NonNull AxisRange range,
      @NonNull List<@NonNull BorderlineEvidence> evidence
  ) {
    if (axisValue.min() == null || axisValue.max() == null) {
      return;
    }

    if (!range.contains(axisValue.min()) || !range.contains(axisValue.max())) {  // NOSONAR
      evidence.add(new BorderlineEvidence(axisValue.axis(), BorderlineReason.RANGE_OVERLAPS_BOUNDARY));
    }
  }

  private static void detectStandardDeviationOverlap(
      @NonNull AxisValue axisValue,
      @NonNull AxisRange range,
      @NonNull List<@NonNull BorderlineEvidence> evidence
  ) {
    if (axisValue.standardDeviation() == null) {
      return;
    }

    final var lower = axisValue.value().subtract(axisValue.standardDeviation());
    final var upper = axisValue.value().add(axisValue.standardDeviation());

    if (!range.contains(lower) || !range.contains(upper)) {
      evidence.add(new BorderlineEvidence(
          axisValue.axis(),
          BorderlineReason.STANDARD_DEVIATION_OVERLAPS_BOUNDARY
      ));
    }
  }

  private static boolean isCloseTo(
      @NonNull BigDecimal value,
      @org.jspecify.annotations.Nullable BigDecimal boundary
  ) {
    if (boundary == null || boundary.signum() == 0) {
      return false;
    }

    final var margin = boundary.abs().multiply(DEFAULT_BOUNDARY_MARGIN_RATIO);
    return isAtMost(value.subtract(boundary).abs(), margin);
  }

  private static @NonNull ClassificationConfidence confidenceFrom(
      @NonNull List<@NonNull BorderlineEvidence> evidence,
      @NonNull ChemicalProfileAxes axes
  ) {
    if (!evidence.isEmpty()) {
      return ClassificationConfidence.LOW;
    }

    final var hasOnlyExactValues = Stream.of(
            ChemicalAxis.SUGAR,
            ChemicalAxis.ACIDITY,
            ChemicalAxis.TANNINITY
        )
        .flatMap(axis -> axes.valueOf(axis).stream())
        .allMatch(value -> value.valueKind() == MeasurementValueKind.EXACT);

    return hasOnlyExactValues
           ? ClassificationConfidence.HIGH
           : ClassificationConfidence.MEDIUM;
  }

  public @NonNull ClassificationAssessment assess(
      @NonNull ClassificationRule<?> matchedRule,
      @NonNull ChemicalProfileAxes axes
  ) {
    requireNonNull(matchedRule, "matched classification rule cannot be null");
    requireNonNull(axes, "chemical profile axes cannot be null");

    final var evidence = new ArrayList<BorderlineEvidence>();

    for (final var entry : matchedRule.ranges().entrySet()) {
      final var axis = entry.getKey();
      final var range = entry.getValue();
      final var value = axes.valueOf(axis);

      if (value.isEmpty()) {
        evidence.add(new BorderlineEvidence(axis, BorderlineReason.MISSING_AXIS));
        continue;
      }

      detectCloseToBoundary(value.orElseThrow(), range, evidence);
      detectRangeOverlap(value.orElseThrow(), range, evidence);
      detectStandardDeviationOverlap(value.orElseThrow(), range, evidence);
    }

    final var confidence = confidenceFrom(evidence, axes);

    return new ClassificationAssessment(
        confidence,
        !evidence.isEmpty(),
        evidence
    );
  }
}
