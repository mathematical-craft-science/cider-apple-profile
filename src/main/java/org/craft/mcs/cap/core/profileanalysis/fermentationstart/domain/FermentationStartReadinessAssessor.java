package org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhFermentationStartPolicy;
import org.craft.mcs.cap.core.profileanalysis.yan.domain.YanFermentationStartPolicy;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Rule-based fermentation-start readiness assessor for feedstock juice/must.
 *
 * <p>This strategy intentionally uses conservative, explainable thresholds instead of a yeast-aware
 * fermentation model. It checks only the current chemical profile dimensions that matter before a
 * cidermaker starts fermentation: sugar availability, pH risk visibility, and yeast-assimilable
 * nitrogen. Future yeast catalog and fermentation-plan work can replace this assessor through the
 * same application boundary with a strain-aware strategy.
 *
 * <p>pH assessment is delegated to {@link PhFermentationStartPolicy}. The default policy is a
 * curated cider business rule, not a physical proof that fermentation can or cannot start.
 *
 * <p>YAN assessment is delegated to {@link YanFermentationStartPolicy}. The default policy is a
 * curated cider/enology business rule that must later become strain-, gravity-, nutrient-plan- and
 * method-aware.
 */
@Service
public final class FermentationStartReadinessAssessor {

  public static final @NonNull String METHOD = "RULE_BASED_CIDER_READINESS_V1";

  private static final @NonNull String SUGAR_UNIT = BeverageUnits.label(BeverageUnits.SG);

  private final @NonNull PhFermentationStartPolicy phPolicy;
  private final @NonNull YanFermentationStartPolicy yanPolicy;

  public FermentationStartReadinessAssessor() {
    this(PhFermentationStartPolicy.curatedCiderV1(), YanFermentationStartPolicy.curatedCiderV1());
  }

  public FermentationStartReadinessAssessor(
      @NonNull PhFermentationStartPolicy phPolicy,
      @NonNull YanFermentationStartPolicy yanPolicy
  ) {
    this.phPolicy = requireNonNull(phPolicy, "pH fermentation-start policy cannot be null");
    this.yanPolicy = requireNonNull(yanPolicy, "YAN fermentation-start policy cannot be null");
  }

  public @NonNull FermentationStartReadinessAssessment assess(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    final var checks = new ArrayList<FermentationStartReadinessCheck>();
    checks.add(sugarCheck(profile));
    checks.add(phPolicy.check(profile));
    checks.add(yanPolicy.check(profile));

    return new FermentationStartReadinessAssessment(statusOf(checks), METHOD, checks);
  }

  private static @NonNull FermentationStartReadinessCheck sugarCheck(@NonNull ChemicalProfile profile) {
    if (profile.sugar().isEmpty()) {
      return check(
          FermentationStartReadinessFactor.SUGAR,
          FermentationStartReadinessSeverity.BLOCKER,
          FermentationStartReadinessCheckCode.SUGAR_MISSING,
          "Sugar measurement is required before fermentation start assessment.",
          null,
          SUGAR_UNIT
      );
    }

    return check(
        FermentationStartReadinessFactor.SUGAR,
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.SUGAR_PRESENT,
        "Sugar measurement is present and can support fermentation planning.",
        null,
        SUGAR_UNIT
    );
  }

  private static @NonNull FermentationStartReadinessStatus statusOf(
      @NonNull List<@NonNull FermentationStartReadinessCheck> checks
  ) {
    if (checks.stream().anyMatch(check -> check.severity() == FermentationStartReadinessSeverity.BLOCKER)) {
      return FermentationStartReadinessStatus.NOT_READY;
    }
    if (checks.stream().anyMatch(check -> check.severity() == FermentationStartReadinessSeverity.WARNING)) {
      return FermentationStartReadinessStatus.NEEDS_ATTENTION;
    }
    return FermentationStartReadinessStatus.READY;
  }

  private static @NonNull FermentationStartReadinessCheck check(
      @NonNull FermentationStartReadinessFactor factor,
      @NonNull FermentationStartReadinessSeverity severity,
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull String message,
      @Nullable BigDecimal value,
      @Nullable String unit
  ) {
    return new FermentationStartReadinessCheck(factor, severity, code, message, value, unit);
  }
}
