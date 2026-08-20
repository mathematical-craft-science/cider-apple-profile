package org.craft.mcs.cap.core.cideranalysis.readiness.domain.planning;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ReadinessRuleSource;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph.CiderPhGuidanceRange;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Generic cider pH guidance used before a concrete fermentation plan exists.
 *
 * <p>This policy is a curated CAP business rule, not a physical fermentation boundary. It helps the
 * technologist notice likely yeast-stress or microbial-risk areas while keeping the final decision
 * with the technologist.
 */
@Service
public record PhPlanningGuidancePolicy(
    @NonNull CiderPhGuidanceRange idealRange,
    @NonNull ReadinessRuleSource source
) {

  public PhPlanningGuidancePolicy {
    requireNonNull(idealRange, "pH planning guidance ideal range cannot be null");
    requireNonNull(source, "pH planning guidance source cannot be null");
  }

  public static @NonNull PhPlanningGuidancePolicy curatedCiderV1() {
    return new PhPlanningGuidancePolicy(
        CiderPhGuidanceRange.curatedCiderV1(),
        ReadinessRuleSource.curatedPhV1()
    );
  }

  public @NonNull PhPlanningGuidance assess(
      @Nullable CharacteristicValue<PhValue> ph
  ) {
    final var value = ph == null ? null : representativeValue(ph);
    if (value == null) {
      return guidance(
          PhPlanningGuidanceCode.PH_MISSING,
          "pH is missing; fermentation pH risk cannot be assessed.",
          null
      );
    }
    if (idealRange.isBelow(value)) {
      return guidance(
          PhPlanningGuidanceCode.PH_PLANNING_LOW,
          "pH is low for generic cider planning; select tolerant yeast or define a preparation plan.",
          value
      );
    }
    if (idealRange.isAbove(value)) {
      return guidance(
          PhPlanningGuidanceCode.PH_PLANNING_HIGH,
          "pH is high for generic cider planning; review microbial risk, SO2 strategy or acid adjustment.",
          value
      );
    }

    return guidance(
        PhPlanningGuidanceCode.PH_PLANNING_IDEAL,
        "pH is within the curated cider planning guidance range.",
        value
    );
  }

  private @NonNull PhPlanningGuidance guidance(
      @NonNull PhPlanningGuidanceCode code,
      @NonNull String message,
      @Nullable BigDecimal value
  ) {
    return new PhPlanningGuidance(code, message, value, source);
  }

  private static @NonNull BigDecimal representativeValue(
      @NonNull CharacteristicValue<PhValue> characteristic
  ) {
    return midpoint(characteristic);
  }
}
