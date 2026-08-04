package org.craft.mcs.cap.core.profileanalysis.graph.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isAtMost;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Reusable ellipse in the SG/TA coordinate space. */
@ValueObject
public record EllipseSugarAcidityGeometry(
    @NonNull SpecificGravityValue specificGravityCenter,
    @NonNull TitratableAcidityValue titratableAcidityCenter,
    @NonNull BigDecimal specificGravityRadius,
    @NonNull BigDecimal titratableAcidityRadius
) implements SugarAcidityRegion {

  private static final MathContext MC = MathContext.DECIMAL64;
  private static final int BOUNDARY_SAMPLE_COUNT = 256;
  private static final int REFINEMENT_ITERATIONS = 48;
  private static final double FULL_TURN = Math.PI * 2.0;
  private static final double GOLDEN_RATIO_CONJUGATE = (Math.sqrt(5.0) - 1.0) / 2.0;

  public EllipseSugarAcidityGeometry {
    requireNonNull(specificGravityCenter, "ellipse specific gravity center cannot be null");
    requireNonNull(titratableAcidityCenter, "ellipse titratable acidity center cannot be null");
    requireNonNull(specificGravityRadius, "ellipse specific gravity radius cannot be null");
    requireNonNull(titratableAcidityRadius, "ellipse titratable acidity radius cannot be null");
    if (!isPositive(specificGravityRadius)) {
      throw new DomainRuleViolationException("ellipse specific gravity radius must be positive");
    }
    if (!isPositive(titratableAcidityRadius)) {
      throw new DomainRuleViolationException("ellipse titratable acidity radius must be positive");
    }
  }

  @Override
  public @NonNull SugarAcidityGeometryShape shape() {
    return SugarAcidityGeometryShape.ELLIPSE;
  }

  @Override
  public boolean contains(@NonNull SugarAcidityGraphPoint point) {
    requireNonNull(point, "SG/TA point cannot be null");
    return isAtMost(normalizedSquaredDistance(point), BigDecimal.ONE);
  }

  /** Selects a boundary point using the caller's authoritative distance policy. */
  public @NonNull SugarAcidityGraphPoint nearestPointTo(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull SugarAcidityPointDistance distance
  ) {
    requireNonNull(point, "SG/TA point cannot be null");
    requireNonNull(distance, "SG/TA point distance cannot be null");
    if (contains(point)) {
      return point;
    }

    final var sampleStep = FULL_TURN / BOUNDARY_SAMPLE_COUNT;
    var best = boundaryCandidate(point, distance, 0.0);
    for (int sample = 1; sample < BOUNDARY_SAMPLE_COUNT; sample++) {
      best = nearer(best, boundaryCandidate(point, distance, sample * sampleStep));
    }
    return refine(point, distance, best.angle() - sampleStep, best.angle() + sampleStep).point();
  }

  private @NonNull BoundaryCandidate refine(
      @NonNull SugarAcidityGraphPoint source,
      @NonNull SugarAcidityPointDistance distance,
      double lowerAngle,
      double upperAngle
  ) {
    var left = lowerAngle;
    var right = upperAngle;
    var leftCandidate = boundaryCandidate(
        source,
        distance,
        right - GOLDEN_RATIO_CONJUGATE * (right - left)
    );
    var rightCandidate = boundaryCandidate(
        source,
        distance,
        left + GOLDEN_RATIO_CONJUGATE * (right - left)
    );
    for (int iteration = 0; iteration < REFINEMENT_ITERATIONS; iteration++) {
      if (leftCandidate.distance().compareTo(rightCandidate.distance()) <= 0) {
        right = rightCandidate.angle();
        rightCandidate = leftCandidate;
        leftCandidate = boundaryCandidate(
            source,
            distance,
            right - GOLDEN_RATIO_CONJUGATE * (right - left)
        );
      } else {
        left = leftCandidate.angle();
        leftCandidate = rightCandidate;
        rightCandidate = boundaryCandidate(
            source,
            distance,
            left + GOLDEN_RATIO_CONJUGATE * (right - left)
        );
      }
    }
    return nearer(leftCandidate, rightCandidate);
  }

  private @NonNull BoundaryCandidate boundaryCandidate(
      @NonNull SugarAcidityGraphPoint source,
      @NonNull SugarAcidityPointDistance distance,
      double angle
  ) {
    final var point = boundaryPoint(angle);
    final var candidateDistance = requireNonNull(
        distance.between(source, point),
        "SG/TA point distance result cannot be null"
    );
    if (candidateDistance.signum() < 0) {
      throw new DomainRuleViolationException("SG/TA point distance cannot be negative");
    }
    return new BoundaryCandidate(angle, point, candidateDistance);
  }

  private @NonNull SugarAcidityGraphPoint boundaryPoint(double angle) {
    final var specificGravity = specificGravityCenter.asBigDecimal().add(
        specificGravityRadius.multiply(BigDecimal.valueOf(Math.cos(angle)), MC),
        MC
    );
    final var titratableAcidity = titratableAcidityCenter.asBigDecimal().add(
        titratableAcidityRadius.multiply(BigDecimal.valueOf(Math.sin(angle)), MC),
        MC
    );
    return new SugarAcidityGraphPoint(
        SpecificGravityValue.of(specificGravity),
        TitratableAcidityValue.of(titratableAcidity)
    );
  }

  private static @NonNull BoundaryCandidate nearer(
      @NonNull BoundaryCandidate first,
      @NonNull BoundaryCandidate second
  ) {
    return first.distance().compareTo(second.distance()) <= 0 ? first : second;
  }

  private @NonNull BigDecimal normalizedSquaredDistance(
      @NonNull SugarAcidityGraphPoint point
  ) {
    final var normalizedSpecificGravity = point.specificGravity().asBigDecimal()
        .subtract(specificGravityCenter.asBigDecimal(), MC)
        .divide(specificGravityRadius, MC);
    final var normalizedTitratableAcidity = point.titratableAcidity().asBigDecimal()
        .subtract(titratableAcidityCenter.asBigDecimal(), MC)
        .divide(titratableAcidityRadius, MC);
    return normalizedSpecificGravity.multiply(normalizedSpecificGravity, MC)
        .add(
            normalizedTitratableAcidity.multiply(normalizedTitratableAcidity, MC),
            MC
        );
  }

  private record BoundaryCandidate(
      double angle,
      @NonNull SugarAcidityGraphPoint point,
      @NonNull BigDecimal distance
  ) {

    private BoundaryCandidate {
      requireNonNull(point, "ellipse boundary point cannot be null");
      requireNonNull(distance, "ellipse boundary distance cannot be null");
    }
  }
}
