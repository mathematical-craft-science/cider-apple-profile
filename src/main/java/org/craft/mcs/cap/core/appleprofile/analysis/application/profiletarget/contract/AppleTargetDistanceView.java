package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/** Explainable target distance owned by Apple catalog analysis. */
public record AppleTargetDistanceView(
    @NonNull Status status,
    @NonNull Coordinates point,
    @NonNull Coordinates nearestTargetPoint,
    @Nullable TanninValue tannin,
    @Nullable TanninValue nearestTargetTannin,
    @Nullable BigDecimal value,
    @Nullable Integer matchPercent,
    @NonNull FitStatus fitStatus,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull ObjectiveFunction objectiveFunction,
    @NonNull Metric metric,
    @NonNull TargetKind targetKind,
    @NonNull GeometryShape targetShape,
    @NonNull Set<@NonNull EvaluationAxis> missingAxes
) {

  public AppleTargetDistanceView {
    requireNonNull(status, "apple target distance status cannot be null");
    requireNonNull(point, "apple target point cannot be null");
    requireNonNull(nearestTargetPoint, "apple nearest target point cannot be null");
    requireNonNull(fitStatus, "apple target fit status cannot be null");
    requireNonNull(objectiveFunction, "apple target objective function cannot be null");
    requireNonNull(metric, "apple target metric cannot be null");
    requireNonNull(targetKind, "apple target kind cannot be null");
    requireNonNull(targetShape, "apple target shape cannot be null");
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "apple missing target axes cannot be null"));
    validateStatus(status, value, matchPercent, fitStatus, insideTarget, missingAxes);
    if (insideTarget && !insideSugarAcidityTarget) {
      throw new ApplicationValidationException(
          "full apple target containment requires SG/TA containment"
      );
    }
    if ((tannin == null) != (nearestTargetTannin == null)) {
      throw new ApplicationValidationException(
          "apple candidate and nearest target tannin must be present together"
      );
    }
    if (value != null && isNegative(value)) {
      throw new ApplicationValidationException("apple target distance cannot be negative");
    }
    if (insideTarget && (value == null || value.signum() != 0 || !Integer.valueOf(100).equals(matchPercent))) {
      throw new ApplicationValidationException(
          "an apple candidate inside the target must have zero distance and 100% fit"
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
      throw new ApplicationValidationException("apple target match percent must be between 0 and 100");
    }
    if (status == Status.SCORED) {
      if (value == null || matchPercent == null || fitStatus == FitStatus.UNSCORABLE || !missingAxes.isEmpty()) {
        throw new ApplicationValidationException("scored apple target distance requires complete scoring facts");
      }
      return;
    }
    if (value != null || matchPercent != null || fitStatus != FitStatus.UNSCORABLE
        || missingAxes.isEmpty() || insideTarget) {
      throw new ApplicationValidationException("unscorable apple target distance requires missing axes only");
    }
  }

  public record Coordinates(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity
  ) {

    public Coordinates {
      requireNonNull(specificGravity, "apple target coordinate specific gravity cannot be null");
      requireNonNull(titratableAcidity, "apple target coordinate titratable acidity cannot be null");
    }
  }

  public enum Status { SCORED, UNSCORABLE }
  public enum FitStatus { EXCELLENT, CLOSE, NEEDS_ADJUSTMENT, UNSCORABLE }
  public enum ObjectiveFunction { DISTANCE_TO_TARGET_AREA, MANHATTAN_DISTANCE_TO_TARGET_AREA }
  public enum Metric { NORMALIZED_WEIGHTED_EUCLIDEAN, NORMALIZED_WEIGHTED_MANHATTAN }
  public enum TargetKind { PREDEFINED_ZONE, CUSTOM_RECTANGLE }
  public enum GeometryShape { RECTANGLE, ELLIPSE }
  public enum EvaluationAxis { SPECIFIC_GRAVITY, TITRATABLE_ACIDITY, TANNIN }
}
