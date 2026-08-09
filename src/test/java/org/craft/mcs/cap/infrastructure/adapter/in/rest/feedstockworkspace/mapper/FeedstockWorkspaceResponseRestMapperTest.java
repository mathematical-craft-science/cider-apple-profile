package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningView;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedstockWorkspaceResponseRestMapperTest {

  @Test
  void mapsCurrentProfileSummaryForPlanningBoardRows() {
    final var response = FeedstockWorkspaceResponseRestMapper.toResponse(new FeedstockPlanningView(
        List.of(batchView()),
        null
    ));

    assertThat(response.batches())
        .singleElement()
        .satisfies(row -> assertThat(row.profileSummary())
            .isNotNull()
            .satisfies(summary -> {
              assertProfileValue(
                  summary.specificGravity(),
                  "1.052",
                  CanonicalUnitLabels.SPECIFIC_GRAVITY,
                  ProfileCharacteristicValueKind.POINT
              );
              assertProfileValue(
                  summary.titratableAcidity(),
                  "6.0",
                  CanonicalUnitLabels.TITRATABLE_ACIDITY,
                  ProfileCharacteristicValueKind.INTERVAL_MIDPOINT
              );
              assertProfileValue(
                  summary.ph(),
                  "3.45",
                  CanonicalUnitLabels.PH,
                  ProfileCharacteristicValueKind.POINT
              );
              assertProfileValue(
                  summary.tannin(),
                  "1500",
                  CanonicalUnitLabels.TANNIN,
                  ProfileCharacteristicValueKind.INTERVAL_MIDPOINT
              );
              assertProfileValue(
                  summary.yan(),
                  "110",
                  CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN,
                  ProfileCharacteristicValueKind.POINT
              );
            }));
  }

  private static @NonNull FeedstockPlanningBatchView batchView() {
    return new FeedstockPlanningBatchView(
        batch(),
        new FeedstockCurrentProfileSummaryView(
            new FeedstockLatestMeasurementSetView(
                MeasurementSetId.newId().value(),
                "Current profile",
                Instant.parse("2026-05-20T10:00:00Z"),
                0,
                null
            ),
            PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
            IntervalCharacteristicValue.of(
                TitratableAcidityValue.of("5.0"),
                TitratableAcidityValue.of("7.0")
            ),
            PointCharacteristicValue.of(PhValue.of("3.45")),
            IntervalCharacteristicValue.of(
                TanninValue.of("1400"),
                TanninValue.of("1600")
            ),
            PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("110"))
        ),
        new FeedstockCurrentProfileAssessmentView(
            List.of(),
            null,
            null,
            null,
            null,
            new FeedstockPlanningReadinessView(FeedstockPlanningReadinessStatus.READY, List.of()),
            new FeedstockCurrentProfileDataQualityView(FeedstockCurrentProfileDataQualityStatus.READY, List.of()),
            null
        ),
        null
    );
  }

  private static @NonNull FeedstockBatchView batch() {
    final var summary = new FeedstockBatchSummary(
        FeedstockBatchId.newId(),
        WorkspaceId.of(UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b")),
        BatchName.of("Late juice"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("100.0"),
        MaterialStateId.newId(),
        null
    );
    return new FeedstockBatchView(
        summary,
        VesselTestFixtures.reference(VesselTestFixtures.FEEDSTOCK_VESSEL_ID, "Tank A-01")
    );
  }

  private static void assertProfileValue(
      @Nullable FeedstockCurrentProfileValueResponse value,
      @NonNull String expectedValue,
      @NonNull String expectedUnit,
      @NonNull ProfileCharacteristicValueKind expectedValueKind
  ) {
    assertThat(value)
        .isNotNull()
        .returns(expectedValueKind, FeedstockCurrentProfileValueResponse::valueKind)
        .satisfies(profileValue -> assertThat(profileValue.value())
            .returns(expectedUnit, DecimalQuantityResponse::unit)
            .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo(expectedValue)));
  }
}
