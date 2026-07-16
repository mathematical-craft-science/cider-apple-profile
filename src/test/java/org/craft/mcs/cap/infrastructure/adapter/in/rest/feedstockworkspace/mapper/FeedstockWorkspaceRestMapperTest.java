package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningView;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceProfileValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedstockWorkspaceRestMapperTest {

  @Test
  void mapsCurrentProfileSummaryForPlanningBoardRows() {
    final var response = FeedstockWorkspaceRestMapper.toResponse(new FeedstockPlanningView(
        List.of(batchView()),
        null
    ));

    assertThat(response.batches())
        .singleElement()
        .satisfies(row -> assertThat(row.profileSummary())
            .isNotNull()
            .satisfies(summary -> {
              assertProfileValue(
                  summary.sugar(),
                  "1.052",
                  CanonicalUnitLabels.SUGAR,
                  MeasurementValueKind.EXACT
              );
              assertProfileValue(
                  summary.titratableAcidity(),
                  "6.0",
                  CanonicalUnitLabels.TITRATABLE_ACIDITY,
                  MeasurementValueKind.RANGE_MIDPOINT
              );
              assertProfileValue(
                  summary.ph(),
                  "3.45",
                  CanonicalUnitLabels.PH,
                  MeasurementValueKind.EXACT
              );
              assertProfileValue(
                  summary.tannin(),
                  "1500",
                  CanonicalUnitLabels.TANNINITY,
                  MeasurementValueKind.STATISTICAL_MEDIAN
              );
              assertProfileValue(
                  summary.yan(),
                  "110",
                  CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN,
                  MeasurementValueKind.EXACT
              );
            }));
  }

  private static @NonNull FeedstockPlanningBatchView batchView() {
    return new FeedstockPlanningBatchView(
        batch(),
        new FeedstockCurrentMeasurementView(
            FeedstockMeasurementId.newId(),
            FeedstockMeasurementName.of("Current profile"),
            Instant.parse("2026-05-20T10:00:00Z"),
            profile(),
            null
        ),
        null,
        null,
        null,
        null,
        new FeedstockPlanningReadinessView(FeedstockPlanningReadinessStatus.READY, List.of()),
        new FeedstockCurrentProfileDataQualityView(FeedstockCurrentProfileDataQualityStatus.READY, List.of()),
        null,
        null
    );
  }

  private static @NonNull FeedstockBatchSummary batch() {
    return new FeedstockBatchSummary(
        FeedstockBatchId.newId(),
        WorkspaceId.of(UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b")),
        FeedstockBatchName.of("Late juice"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        null,
        VolumeValue.ofLiters("100.0"),
        null
    );
  }

  private static @NonNull ChemicalProfile profile() {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
        .titratableAcidity(RangeMeasurement.of(
            TitratableAcidityValue.of("5.0"),
            TitratableAcidityValue.of("7.0")
        ))
        .ph(ExactMeasurement.of(PhValue.of("3.45")))
        .tanninity(StatisticalMeasurement.of(
            TanninityValue.of("1400"),
            TanninityValue.of("1500"),
            new BigDecimal("120"),
            4
        ))
        .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of("110")))
        .build();
  }

  private static void assertProfileValue(
      @Nullable FeedstockWorkspaceProfileValueResponse value,
      @NonNull String expectedValue,
      @NonNull String expectedUnit,
      @NonNull MeasurementValueKind expectedValueKind
  ) {
    assertThat(value)
        .isNotNull()
        .returns(expectedValueKind, FeedstockWorkspaceProfileValueResponse::valueKind)
        .satisfies(profileValue -> assertThat(profileValue.value())
            .returns(expectedUnit, DecimalQuantityResponse::unit)
            .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo(expectedValue)));
  }
}
