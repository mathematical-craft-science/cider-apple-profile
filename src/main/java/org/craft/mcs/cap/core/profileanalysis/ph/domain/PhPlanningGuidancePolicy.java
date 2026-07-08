package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/**
 * Generic cider pH guidance used before a concrete fermentation plan exists.
 *
 * <p>This policy is a curated CAP business rule, not a physical fermentation boundary. It helps the
 * technologist notice likely yeast-stress or microbial-risk areas while keeping the final decision
 * with the technologist.
 */
@Service
public record PhPlanningGuidancePolicy(
    @NonNull BigDecimal idealMin,
    @NonNull BigDecimal idealMax,
    @NonNull PhRuleSource source
) {

  private static final @NonNull BigDecimal DEFAULT_IDEAL_MIN = new BigDecimal("3.3");
  private static final @NonNull BigDecimal DEFAULT_IDEAL_MAX = new BigDecimal("3.8");

  public PhPlanningGuidancePolicy {
    requireNonNull(idealMin, "pH planning guidance ideal minimum cannot be null");
    requireNonNull(idealMax, "pH planning guidance ideal maximum cannot be null");
    requireNonNull(source, "pH planning guidance source cannot be null");
    if (isGreaterThan(idealMin, idealMax)) {
      throw new DomainRuleViolationException("pH planning guidance minimum must not exceed maximum");
    }
  }

  public static @NonNull PhPlanningGuidancePolicy curatedCiderV1() {
    return new PhPlanningGuidancePolicy(
        DEFAULT_IDEAL_MIN,
        DEFAULT_IDEAL_MAX,
        PhRuleSource.curatedCiderV1()
    );
  }

  public @NonNull PhPlanningGuidance assess(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");
    final var value = PhMeasurementValueExtractor.representativeValue(profile);
    if (value == null) {
      return guidance(
          PhPlanningGuidanceCode.PH_MISSING,
          "pH is missing; fermentation pH risk cannot be assessed.",
          null
      );
    }
    if (isLessThan(value, idealMin)) {
      return guidance(
          PhPlanningGuidanceCode.PH_PLANNING_LOW,
          "pH is low for generic cider planning; select tolerant yeast or define a preparation plan.",
          value
      );
    }
    if (isGreaterThan(value, idealMax)) {
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
}
