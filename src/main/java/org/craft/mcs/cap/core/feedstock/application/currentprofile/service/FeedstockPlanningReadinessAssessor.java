package org.craft.mcs.cap.core.feedstock.application.currentprofile.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSignalSeverity;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheckCode;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidance;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidanceCode;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Assesses whether current Feedstock facts are sufficient for composition and target planning.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockPlanningReadinessAssessor {

  private final @NonNull PhPlanningGuidancePolicy phPlanningGuidancePolicy;

  public @NonNull FeedstockPlanningReadinessView assessWithoutCurrentProfile(
      @NonNull FeedstockBatchSummary batch
  ) {
    final var checks = new ArrayList<FeedstockPlanningReadinessCheck>();
    checks.add(volumeCheck(batch));
    checks.add(requiredProfileCheck(
        false,
        FeedstockPlanningReadinessFactor.SPECIFIC_GRAVITY,
        FeedstockPlanningReadinessCheckCode.SPECIFIC_GRAVITY_PRESENT,
        FeedstockPlanningReadinessCheckCode.SPECIFIC_GRAVITY_MISSING,
        "Specific gravity is available",
        "Specific gravity is required for composition planning"
    ));
    checks.add(requiredProfileCheck(
        false,
        FeedstockPlanningReadinessFactor.TITRATABLE_ACIDITY,
        FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_PRESENT,
        FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_MISSING,
        "Titratable acidity is available",
        "Titratable acidity is required for composition planning"
    ));
    checks.add(tanninCheck(false));
    checks.add(phGuidanceCheck(phPlanningGuidancePolicy.assess(null)));
    return new FeedstockPlanningReadinessView(status(checks), checks);
  }

  public @NonNull FeedstockPlanningReadinessView assess(
      @NonNull FeedstockBatchSummary batch,
      @NonNull FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentProfile, "feedstock current profile cannot be null");

    final var checks = new ArrayList<FeedstockPlanningReadinessCheck>();
    checks.add(volumeCheck(batch));
    checks.add(requiredProfileCheck(
        currentProfile.specificGravity() != null,
        FeedstockPlanningReadinessFactor.SPECIFIC_GRAVITY,
        FeedstockPlanningReadinessCheckCode.SPECIFIC_GRAVITY_PRESENT,
        FeedstockPlanningReadinessCheckCode.SPECIFIC_GRAVITY_MISSING,
        "Specific gravity is available",
        "Specific gravity is required for composition planning"
    ));
    checks.add(requiredProfileCheck(
        currentProfile.titratableAcidity() != null,
        FeedstockPlanningReadinessFactor.TITRATABLE_ACIDITY,
        FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_PRESENT,
        FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_MISSING,
        "Titratable acidity is available",
        "Titratable acidity is required for composition planning"
    ));
    checks.add(tanninCheck(currentProfile.tannin() != null));
    checks.add(phGuidanceCheck(
        phPlanningGuidancePolicy.assess(currentProfile.ph())
    ));
    return new FeedstockPlanningReadinessView(status(checks), checks);
  }

  private static @NonNull FeedstockPlanningReadinessCheck volumeCheck(
      @NonNull FeedstockBatchSummary batch
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    return check(
        FeedstockPlanningReadinessFactor.VOLUME,
        batch.availableVolume().isZero()
            ? FeedstockCurrentProfileSignalSeverity.BLOCKING
            : FeedstockCurrentProfileSignalSeverity.OK,
        batch.availableVolume().isZero()
            ? FeedstockPlanningReadinessCheckCode.AVAILABLE_VOLUME_EMPTY
            : FeedstockPlanningReadinessCheckCode.AVAILABLE_VOLUME_PRESENT,
        batch.availableVolume().isZero()
            ? "Available volume is zero"
            : "Available volume is present"
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheck tanninCheck(boolean present) {
    return check(
        FeedstockPlanningReadinessFactor.TANNIN,
        present
            ? FeedstockCurrentProfileSignalSeverity.OK
            : FeedstockCurrentProfileSignalSeverity.WARNING,
        present
            ? FeedstockPlanningReadinessCheckCode.TANNIN_PRESENT
            : FeedstockPlanningReadinessCheckCode.TANNIN_MISSING,
        present
            ? "Tannin is available"
            : "Tannin is missing; target-aware structure assessment will be incomplete"
    );
  }

  @SuppressWarnings("java:S107")
  private static @NonNull FeedstockPlanningReadinessCheck requiredProfileCheck(
      boolean present,
      @NonNull FeedstockPlanningReadinessFactor factor,
      @NonNull FeedstockPlanningReadinessCheckCode presentCode,
      @NonNull FeedstockPlanningReadinessCheckCode missingCode,
      @NonNull String presentMessage,
      @NonNull String missingMessage
  ) {
    return check(
        factor,
        present ? FeedstockCurrentProfileSignalSeverity.OK : FeedstockCurrentProfileSignalSeverity.BLOCKING,
        present ? presentCode : missingCode,
        present ? presentMessage : missingMessage
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheck phGuidanceCheck(
      @NonNull PhPlanningGuidance guidance
  ) {
    requireNonNull(guidance, "pH planning guidance cannot be null");
    return check(
        FeedstockPlanningReadinessFactor.PH,
        phSeverity(guidance.code()),
        phCode(guidance.code()),
        guidance.message()
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheckCode phCode(
      @NonNull PhPlanningGuidanceCode code
  ) {
    return switch (requireNonNull(code, "pH planning guidance code cannot be null")) {
      case PH_PLANNING_IDEAL -> FeedstockPlanningReadinessCheckCode.PH_PLANNING_IDEAL;
      case PH_PLANNING_LOW -> FeedstockPlanningReadinessCheckCode.PH_PLANNING_LOW;
      case PH_PLANNING_HIGH -> FeedstockPlanningReadinessCheckCode.PH_PLANNING_HIGH;
      case PH_MISSING -> FeedstockPlanningReadinessCheckCode.PH_MISSING;
    };
  }

  private static @NonNull FeedstockCurrentProfileSignalSeverity phSeverity(
      @NonNull PhPlanningGuidanceCode code
  ) {
    return switch (requireNonNull(code, "pH planning guidance code cannot be null")) {
      case PH_PLANNING_IDEAL -> FeedstockCurrentProfileSignalSeverity.OK;
      case PH_PLANNING_LOW, PH_PLANNING_HIGH, PH_MISSING -> FeedstockCurrentProfileSignalSeverity.WARNING;
    };
  }

  private static @NonNull FeedstockPlanningReadinessCheck check(
      @NonNull FeedstockPlanningReadinessFactor factor,
      @NonNull FeedstockCurrentProfileSignalSeverity severity,
      @NonNull FeedstockPlanningReadinessCheckCode code,
      @NonNull String message
  ) {
    return new FeedstockPlanningReadinessCheck(factor, severity, code, message);
  }

  private static @NonNull FeedstockPlanningReadinessStatus status(
      @NonNull List<FeedstockPlanningReadinessCheck> checks
  ) {
    if (checks.stream().anyMatch(check -> check.severity() == FeedstockCurrentProfileSignalSeverity.BLOCKING)) {
      return FeedstockPlanningReadinessStatus.BLOCKED;
    }
    if (checks.stream().anyMatch(check -> check.severity() == FeedstockCurrentProfileSignalSeverity.WARNING)) {
      return FeedstockPlanningReadinessStatus.NEEDS_ATTENTION;
    }
    return FeedstockPlanningReadinessStatus.READY;
  }
}
