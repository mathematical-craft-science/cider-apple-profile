package org.craft.mcs.cap.feedstock.application.usecase;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.usecase.QueryFeedstockBatchCurrentSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryFeedstockBatchCurrentSugarAcidityZonePointsServiceTest {

  @Test
  void queriesLatestMeasurementPerBatchForCurrentGraphPoints() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(repository);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var current = measurement("Current", FeedstockBatchId.newId());

    when(repository.findLatestSugarAcidityPointCandidates(any())).thenReturn(List.of(current));

    final var result = service.query(new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        CursorPageRequest.firstPage()
    ));

    verify(repository).findLatestSugarAcidityPointCandidates(any());
    assertThat(result.items())
        .extracting(FeedstockBatchCurrentSugarAcidityZoneResult::currentMeasurementId)
        .containsExactly(current.measurementId());
    assertThat(result.items().getFirst().assessment().zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
  }

  private static QueryFeedstockBatchCurrentSugarAcidityZonePointsService service(
      FeedstockMeasurementQueryRepository repository
  ) {
    return new QueryFeedstockBatchCurrentSugarAcidityZonePointsService(
        new FeedstockBatchCurrentMeasurementQueryService(repository),
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
    );
  }

  private static FeedstockMeasurement measurement(String name, FeedstockBatchId batchId) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of(name),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.066")
            )))
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.7")))
            .build(),
        feedstockMeasurementInstant()
    );
  }
}
