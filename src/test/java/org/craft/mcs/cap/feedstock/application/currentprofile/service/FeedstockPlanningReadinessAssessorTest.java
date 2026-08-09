package org.craft.mcs.cap.feedstock.application.currentprofile.service;

import java.time.LocalDate;
import java.time.Month;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessCheckCode;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockPlanningReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockPlanningReadinessAssessorTest {

  private final FeedstockPlanningReadinessAssessor assessor = new FeedstockPlanningReadinessAssessor(
      PhPlanningGuidancePolicy.curatedCiderV1()
  );

  @Test
  void reportsConcreteMissingCharacteristicsWhenCurrentProfileIsAbsent() {
    final var readiness = assessor.assessWithoutCurrentProfile(batch());

    assertThat(readiness)
        .returns(FeedstockPlanningReadinessStatus.BLOCKED, FeedstockPlanningReadinessView::status)
        .extracting(FeedstockPlanningReadinessView::checks)
        .satisfies(checks -> {
          assertThat(checks)
              .extracting(FeedstockPlanningReadinessCheck::factor)
              .containsExactly(
                  FeedstockPlanningReadinessFactor.VOLUME,
                  FeedstockPlanningReadinessFactor.SPECIFIC_GRAVITY,
                  FeedstockPlanningReadinessFactor.TITRATABLE_ACIDITY,
                  FeedstockPlanningReadinessFactor.TANNIN,
                  FeedstockPlanningReadinessFactor.PH
              );
          assertThat(checks)
              .extracting(FeedstockPlanningReadinessCheck::code)
              .containsExactly(
                  FeedstockPlanningReadinessCheckCode.AVAILABLE_VOLUME_PRESENT,
                  FeedstockPlanningReadinessCheckCode.SPECIFIC_GRAVITY_MISSING,
                  FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_MISSING,
                  FeedstockPlanningReadinessCheckCode.TANNIN_MISSING,
                  FeedstockPlanningReadinessCheckCode.PH_MISSING
              );
        });
  }

  private static FeedstockBatchSummary batch() {
    return new FeedstockBatchSummary(
        FeedstockBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Planning batch"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(
            LocalDate.of(2026, Month.SEPTEMBER, 1),
            LocalDate.of(2026, Month.DECEMBER, 31)
        ),
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("100"),
        MaterialStateId.newId(),
        null
    );
  }
}
