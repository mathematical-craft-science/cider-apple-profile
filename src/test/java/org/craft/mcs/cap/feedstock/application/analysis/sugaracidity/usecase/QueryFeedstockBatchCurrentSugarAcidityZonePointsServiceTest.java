package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.QueryFeedstockBatchCurrentSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
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
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(batchRepository, repository);
    final var workspaceId = WorkspaceId.of(randomUUID());
    final var batch = batch(workspaceId);
    final var current = measurement("Current", batch.batchId());

    when(batchRepository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(batch));
    when(repository.findLatestSugarAcidityPointCandidates(any())).thenReturn(List.of(current));

    final var result = service.query(new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        CursorPageRequest.firstPage()
    ));

    verify(repository).findLatestSugarAcidityPointCandidates(any());
    assertThat(result.items())
        .extracting(FeedstockBatchCurrentSugarAcidityZoneResult::currentMeasurementId)
        .containsExactly(current.measurementId());
    assertThat(result.items().getFirst().batchName()).isEqualTo(batch.name());
    assertThat(result.items().getFirst().measuredAt()).isEqualTo(current.measuredAt());
    assertThat(result.items().getFirst().evaluation().assessment().zone()).isEqualTo(SugarAcidityZone.PERFECT_APPLES);
  }

  private static QueryFeedstockBatchCurrentSugarAcidityZonePointsService service(
      FeedstockBatchRepository batchRepository,
      FeedstockMeasurementQueryRepository repository
  ) {
    return new QueryFeedstockBatchCurrentSugarAcidityZonePointsService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        new FeedstockBatchCurrentMeasurementQueryService(repository),
        projector()
    );
  }

  private static SugarAcidityProfileProjector projector() {
    return new SugarAcidityProfileProjector(
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
    );
  }

  private static FeedstockMeasurement measurement(
      String name,
      FeedstockBatchId batchId
  ) {
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

  private static FeedstockBatch batch(WorkspaceId workspaceId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        FeedstockBatchName.of("Late season juice"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.of(2026, Month.SEPTEMBER, 1), LocalDate.of(2026, Month.DECEMBER, 31)),
        VolumeValue.ofLiters("100")
    );
  }
}
