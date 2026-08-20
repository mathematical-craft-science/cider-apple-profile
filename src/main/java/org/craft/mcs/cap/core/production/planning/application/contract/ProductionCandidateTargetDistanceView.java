package org.craft.mcs.cap.core.production.planning.application.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest.ObjectiveFunction;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/** Explainable target-fit snapshot owned and persisted by Production Planning. */
public record ProductionCandidateTargetDistanceView(
    @NonNull Status status,
    @NonNull Coordinates point,
    @NonNull Coordinates nearestTargetPoint,
    @NonNull TargetPosition targetPosition,
    @Nullable TanninValue tannin,
    @Nullable TanninValue nearestTargetTannin,
    @Nullable BigDecimal value,
    @Nullable Integer matchPercent,
    @NonNull FitStatus fitStatus,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull ObjectiveFunction objectiveFunction,
    @NonNull Metric metric,
    @NonNull Weights weights,
    @NonNull TargetKind targetKind,
    @NonNull GeometryShape targetShape,
    @NonNull Set<@NonNull EvaluationAxis> missingAxes
) {

  public ProductionCandidateTargetDistanceView {
    requireNonNull(status, "production candidate target status cannot be null");
    requireNonNull(point, "production candidate target point cannot be null");
    requireNonNull(nearestTargetPoint, "production candidate nearest target point cannot be null");
    requireNonNull(targetPosition, "production candidate target position cannot be null");
    requireNonNull(fitStatus, "production candidate target fit status cannot be null");
    requireNonNull(objectiveFunction, "production candidate objective function cannot be null");
    requireNonNull(metric, "production candidate target metric cannot be null");
    requireNonNull(weights, "production candidate target weights cannot be null");
    requireNonNull(targetKind, "production candidate target kind cannot be null");
    requireNonNull(targetShape, "production candidate target shape cannot be null");
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "production candidate missing axes cannot be null"));
    validateStatus(status, value, matchPercent, fitStatus, insideTarget, missingAxes);
    if (insideTarget && !insideSugarAcidityTarget) {
      throw new ApplicationValidationException(
          "full production candidate target containment requires SG/TA containment"
      );
    }
    if (insideSugarAcidityTarget != targetPosition.inside()) {
      throw new ApplicationValidationException(
          "production candidate SG/TA containment must agree with target position"
      );
    }
    if ((tannin == null) != (nearestTargetTannin == null)) {
      throw new ApplicationValidationException(
          "production candidate and nearest target tannin must be present together"
      );
    }
    if (value != null && isNegative(value)) {
      throw new ApplicationValidationException("production candidate target distance cannot be negative");
    }
    if (insideTarget && (value == null || value.signum() != 0 || !Integer.valueOf(100).equals(matchPercent))) {
      throw new ApplicationValidationException(
          "a production candidate inside the target must have zero distance and 100% fit"
      );
    }
  }

  private static void validateStatus(
      @NonNull Status status,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      @NonNull FitStatus fitStatus,
      boolean insideTarget,
      @NonNull Set<@NonNull EvaluationAxis> missingAxes
  ) {
    if (matchPercent != null && (matchPercent < 0 || matchPercent > 100)) {
      throw new ApplicationValidationException(
          "production candidate target match percent must be between 0 and 100"
      );
    }
    if (status == Status.SCORED) {
      if (value == null || matchPercent == null || fitStatus == FitStatus.UNSCORABLE || !missingAxes.isEmpty()) {
        throw new ApplicationValidationException(
            "scored production candidate target distance requires complete scoring facts"
        );
      }
      return;
    }
    if (value != null || matchPercent != null || fitStatus != FitStatus.UNSCORABLE
        || missingAxes.isEmpty() || insideTarget) {
      throw new ApplicationValidationException(
          "unscorable production candidate target distance requires missing axes only"
      );
    }
  }

  public record Coordinates(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity
  ) {

    public Coordinates {
      requireNonNull(specificGravity, "production candidate coordinate SG cannot be null");
      requireNonNull(titratableAcidity, "production candidate coordinate TA cannot be null");
    }
  }

  public record TargetPosition(
      @NonNull AxisPosition specificGravity,
      @NonNull AxisPosition titratableAcidity
  ) {

    public TargetPosition {
      requireNonNull(specificGravity, "production candidate SG target position cannot be null");
      requireNonNull(titratableAcidity, "production candidate TA target position cannot be null");
    }

    public boolean inside() {
      return specificGravity == AxisPosition.WITHIN && titratableAcidity == AxisPosition.WITHIN;
    }
  }

  public record Weights(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity,
      @Nullable BigDecimal tannin
  ) {

    public Weights {
      requireNonNull(specificGravity, "production candidate SG weight cannot be null");
      requireNonNull(titratableAcidity, "production candidate TA weight cannot be null");
      if (!isPositive(specificGravity) || !isPositive(titratableAcidity)
          || tannin != null && !isPositive(tannin)) {
        throw new ApplicationValidationException("production candidate target weights must be positive");
      }
    }
  }

  public enum Status { SCORED, UNSCORABLE }
  public enum FitStatus { EXCELLENT, CLOSE, NEEDS_ADJUSTMENT, UNSCORABLE }
  public enum AxisPosition { BELOW, WITHIN, ABOVE }
  public enum Metric { NORMALIZED_WEIGHTED_EUCLIDEAN, NORMALIZED_WEIGHTED_MANHATTAN }
  public enum TargetKind { PREDEFINED_ZONE, CUSTOM_RECTANGLE }
  public enum GeometryShape { RECTANGLE, ELLIPSE }
  public enum EvaluationAxis { SPECIFIC_GRAVITY, TITRATABLE_ACIDITY, TANNIN }
}
