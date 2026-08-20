package org.craft.mcs.cap.core.cideranalysis.readiness.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FermentationStartReadinessAssessmentTest {

  @Test
  void rejectsStatusThatDoesNotMatchCheckSeverities() {
    assertThatThrownBy(() -> new FermentationStartReadinessAssessment(
        FermentationStartReadinessStatus.READY,
        FermentationStartReadinessAssessor.METHOD,
        completeChecks(FermentationStartReadinessSeverity.WARNING)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("fermentation start readiness status must match check severities");
  }

  @Test
  void rejectsMissingOrDuplicateFactorChecks() {
    final var check = check(
        FermentationStartReadinessFactor.SPECIFIC_GRAVITY,
        FermentationStartReadinessSeverity.INFO
    );

    assertThatThrownBy(() -> new FermentationStartReadinessAssessment(
        FermentationStartReadinessStatus.READY,
        FermentationStartReadinessAssessor.METHOD,
        List.of(check, check)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("fermentation start readiness requires exactly one check for every factor");
  }

  @Test
  void rejectsCodeThatDoesNotMatchCheckFactor() {
    assertThatThrownBy(() -> new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.SPECIFIC_GRAVITY,
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.PH_ACCEPTABLE,
        "check",
        null,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("fermentation start readiness code must match its factor");
  }

  @Test
  void rejectsManagedCodeWithoutPreparationAction() {
    assertThatThrownBy(() -> new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.PH,
        FermentationStartReadinessSeverity.WARNING,
        FermentationStartReadinessCheckCode.PH_HIGH_MICROBIAL_RISK_MANAGED,
        "check",
        null,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("managed fermentation start readiness code must match its preparation action");
  }

  @Test
  void rejectsPreparationActionThatDoesNotManageAssessedFactor() {
    assertThatThrownBy(() -> new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
        FermentationStartReadinessSeverity.WARNING,
        FermentationStartReadinessCheckCode.YAN_LOW_MANAGED,
        "check",
        null,
        null,
        FermentationStartPreparationActionType.SO2_ADDITION_CONFIRMED
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("fermentation start readiness preparation action must manage the assessed factor");
  }

  @Test
  void rejectsReadinessValueWithoutUnit() {
    assertThatThrownBy(() -> new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.PH,
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.PH_ACCEPTABLE,
        "check",
        new BigDecimal("3.5"),
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("fermentation start readiness value requires a unit");
  }

  @Test
  void allowsExpectedUnitWhenReadinessValueIsMissing() {
    final var check = new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.PH,
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.PH_ACCEPTABLE,
        "check",
        null,
        "pH"
    );

    assertThat(check)
        .returns(null, FermentationStartReadinessCheck::value)
        .returns("pH", FermentationStartReadinessCheck::unit);
  }

  @Test
  void rejectsBlankReadinessUnit() {
    assertThatThrownBy(() -> new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.PH,
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.PH_ACCEPTABLE,
        "check",
        new BigDecimal("3.5"),
        " "
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("fermentation start readiness unit cannot be blank");
  }

  private static List<FermentationStartReadinessCheck> completeChecks(
      FermentationStartReadinessSeverity phSeverity
  ) {
    return List.of(
        check(FermentationStartReadinessFactor.SPECIFIC_GRAVITY, FermentationStartReadinessSeverity.INFO),
        check(FermentationStartReadinessFactor.PH, phSeverity),
        check(FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN, FermentationStartReadinessSeverity.INFO)
    );
  }

  private static FermentationStartReadinessCheck check(
      FermentationStartReadinessFactor factor,
      FermentationStartReadinessSeverity severity
  ) {
    final var code = switch (factor) {
      case SPECIFIC_GRAVITY -> FermentationStartReadinessCheckCode.SPECIFIC_GRAVITY_PRESENT;
      case PH -> FermentationStartReadinessCheckCode.PH_ACCEPTABLE;
      case YEAST_ASSIMILABLE_NITROGEN -> FermentationStartReadinessCheckCode.YAN_ACCEPTABLE;
    };
    return new FermentationStartReadinessCheck(factor, severity, code, "check", null, null);
  }
}
