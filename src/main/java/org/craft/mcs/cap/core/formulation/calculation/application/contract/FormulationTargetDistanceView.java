package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.ObjectiveFunction;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.Weights;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/** Explainable target-fit facts attached to a calculated composition. */
public record FormulationTargetDistanceView(
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

  public FormulationTargetDistanceView {
    requireNonNull(status, "formulation target status cannot be null");
    requireNonNull(point, "formulation target point cannot be null");
    requireNonNull(nearestTargetPoint, "formulation nearest target point cannot be null");
    requireNonNull(targetPosition, "formulation target position cannot be null");
    requireNonNull(fitStatus, "formulation target fit status cannot be null");
    requireNonNull(objectiveFunction, "formulation target objective function cannot be null");
    requireNonNull(metric, "formulation target metric cannot be null");
    requireNonNull(weights, "formulation target weights cannot be null");
    requireNonNull(targetKind, "formulation target kind cannot be null");
    requireNonNull(targetShape, "formulation target shape cannot be null");
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "formulation missing target axes cannot be null"));
    validateStatus(status, value, matchPercent, fitStatus, insideTarget, missingAxes);
    if (insideTarget && !insideSugarAcidityTarget) {
      throw new ApplicationValidationException(
          "full formulation target containment requires SG/TA containment"
      );
    }
    if (insideSugarAcidityTarget != targetPosition.inside()) {
      throw new ApplicationValidationException(
          "formulation SG/TA containment must agree with target position"
      );
    }
    if ((tannin == null) != (nearestTargetTannin == null)) {
      throw new ApplicationValidationException(
          "formulation candidate and nearest target tannin must be present together"
      );
    }
    if (value != null && isNegative(value)) {
      throw new ApplicationValidationException(
          "formulation target distance cannot be negative"
      );
    }
    if (insideTarget && (value == null || value.signum() != 0 || !Integer.valueOf(100).equals(matchPercent))) {
      throw new ApplicationValidationException(
          "a composition inside the target must have zero distance and 100% fit"
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
          "formulation target match percent must be between 0 and 100"
      );
    }
    if (status == Status.SCORED) {
      if (value == null || matchPercent == null || fitStatus == FitStatus.UNSCORABLE || !missingAxes.isEmpty()) {
        throw new ApplicationValidationException(
            "scored formulation target distance requires complete scoring facts"
        );
      }
      return;
    }
    if (value != null || matchPercent != null || fitStatus != FitStatus.UNSCORABLE
        || missingAxes.isEmpty() || insideTarget) {
      throw new ApplicationValidationException(
          "unscorable formulation target distance requires missing axes only"
      );
    }
  }

  public record TargetPosition(
      @NonNull AxisPosition specificGravity,
      @NonNull AxisPosition titratableAcidity
  ) {

    public TargetPosition {
      requireNonNull(specificGravity, "formulation specific gravity target position cannot be null");
      requireNonNull(titratableAcidity, "formulation titratable acidity target position cannot be null");
    }

    public boolean inside() {
      return specificGravity == AxisPosition.WITHIN && titratableAcidity == AxisPosition.WITHIN;
    }
  }

  public record Coordinates(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity
  ) {

    public Coordinates {
      requireNonNull(specificGravity, "formulation target coordinate specific gravity cannot be null");
      requireNonNull(titratableAcidity, "formulation target coordinate titratable acidity cannot be null");
    }
  }

  public enum Status { SCORED, UNSCORABLE }
  public enum FitStatus { EXCELLENT, CLOSE, NEEDS_ADJUSTMENT, UNSCORABLE }
  public enum Metric { NORMALIZED_WEIGHTED_EUCLIDEAN, NORMALIZED_WEIGHTED_MANHATTAN }
  public enum TargetKind { PREDEFINED_ZONE, CUSTOM_RECTANGLE }
  public enum GeometryShape { RECTANGLE, ELLIPSE }
  public enum EvaluationAxis { SPECIFIC_GRAVITY, TITRATABLE_ACIDITY, TANNIN }
  public enum AxisPosition { BELOW, WITHIN, ABOVE }
}
