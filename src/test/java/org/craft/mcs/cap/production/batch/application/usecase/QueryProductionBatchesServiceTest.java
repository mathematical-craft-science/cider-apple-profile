package org.craft.mcs.cap.production.batch.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionCurrentCharacteristicView;
import org.craft.mcs.cap.core.production.batch.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.batch.application.usecase.ProductionCurrentProfileProjector;
import org.craft.mcs.cap.core.production.batch.application.usecase.ProductionBatchDetailsAssembler;
import org.craft.mcs.cap.core.production.batch.application.usecase.QueryProductionBatchesService;
import org.craft.mcs.cap.core.production.batch.application.usecase.ProductionVesselReferenceResolver;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselReferenceQuery;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;

class QueryProductionBatchesServiceTest {

  private static final PotentialAbvPredictor POTENTIAL_ABV_PREDICTOR = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  @Test
  void returnsAuthorizedProductionBatchView() {
    final var batch = productionBatch();
    final var repository = mock(ProductionBatchQueryRepository.class);
    final var accessControl = mock(ProductionAccessControl.class);
    final var measurementProvider = mock(ProductionMeasurementProfileProvider.class);
    final var service = service(repository, accessControl, measurementProvider);
    when(repository.findById(batch.batchId())).thenReturn(Optional.of(batch));
    when(measurementProvider.findCurrent(batch.batchId())).thenReturn(Optional.empty());

    final var view = service.get(batch.batchId());

    assertThat(view.batch())
        .returns(batch.batchId(), ProductionBatchView::batchId)
        .returns(batch.workspaceId(), ProductionBatchView::workspaceId)
        .returns(batch.name(), ProductionBatchView::name)
        .returns(batch.initialProfileSnapshot(), ProductionBatchView::initialProfileSnapshot);
    verify(accessControl).requireReadAccess(batch.workspaceId());
  }

  @Test
  void returnsOnlyViewsFromAuthorizedWorkspaceList() {
    final var batch = productionBatch();
    final var repository = mock(ProductionBatchQueryRepository.class);
    final var accessControl = mock(ProductionAccessControl.class);
    final var measurementProvider = mock(ProductionMeasurementProfileProvider.class);
    final var service = service(repository, accessControl, measurementProvider);
    when(repository.findAllByWorkspaceId(batch.workspaceId())).thenReturn(List.of(batch));
    when(measurementProvider.findAllCurrent(any(), any())).thenReturn(Map.of());

    final var result = service.list(new ListProductionBatchesQuery(batch.workspaceId()));

    assertThat(result)
        .singleElement()
        .satisfies(details -> assertThat(details.batch())
            .returns(batch.batchId(), ProductionBatchView::batchId)
            .returns(batch.workspaceId(), ProductionBatchView::workspaceId));
    verify(accessControl).requireReadAccess(batch.workspaceId());
  }

  @Test
  void overlaysPartialCurrentMeasurementWithoutHidingInitialAxes() {
    final var batch = productionBatch();
    final var repository = mock(ProductionBatchQueryRepository.class);
    final var accessControl = mock(ProductionAccessControl.class);
    final var measurementProvider = mock(ProductionMeasurementProfileProvider.class);
    final var service = service(repository, accessControl, measurementProvider);
    final var evidence = new MeasurementEvidenceView(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        new MeasurementEvidenceReference(newUuid(), newUuid()),
        Instant.parse("2026-07-24T00:00:00Z"),
        new MeasurementProvenanceView(
            MeasurementSourceType.USER,
            "Hydrometer",
            null,
            null,
            null
        ),
        MeasurementFreshnessStatus.CURRENT
    );
    final var measuredProfile = new ProductionMeasuredProfileView(
        ProductionCurrentCharacteristicView.fromMeasurement(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.035")),
            evidence
        ),
        null,
        null,
        null,
        null
    );
    when(repository.findById(batch.batchId())).thenReturn(Optional.of(batch));
    when(measurementProvider.findCurrent(batch.batchId())).thenReturn(Optional.of(measuredProfile));

    final var details = service.get(batch.batchId());

    assertThat(details.currentProfile())
        .returns(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.035")),
            profile -> profile.specificGravity().value()
        )
        .returns(
            PointCharacteristicValue.of(TitratableAcidityValue.of("6.2")),
            profile -> profile.titratableAcidity().value()
        );
    assertThat(details)
        .satisfies(value -> assertThat(value.currentProfile().measurementEvidence()).containsExactly(evidence))
        .satisfies(value -> assertThat(value.batch().initialProfileSnapshot().specificGravity())
            .isEqualTo(SpecificGravityValue.of("1.052")));
  }

  private static ProductionBatchView productionBatch() {
    return new ProductionBatchView(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Late season cider"),
        VesselTestFixtures.vesselId("Tank F-03"),
        BatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        VolumeValue.ofLiters("120"),
        MaterialStateId.newId(),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("6.2"),
            null,
            null
        ),
        null
    );
  }

  private static QueryProductionBatchesService service(
      ProductionBatchQueryRepository repository,
      ProductionAccessControl accessControl,
      ProductionMeasurementProfileProvider measurementProvider
  ) {
    final var vesselReferenceQuery = mock(ProductionVesselReferenceQuery.class);
    when(vesselReferenceQuery.resolveReferences(any(), anySet())).thenReturn(Map.of(
        VesselTestFixtures.PRODUCTION_VESSEL_ID,
        VesselTestFixtures.reference(VesselTestFixtures.PRODUCTION_VESSEL_ID, "Tank F-03")
    ));
    final var vesselReferenceResolver = new ProductionVesselReferenceResolver(vesselReferenceQuery);
    final var detailsAssembler = new ProductionBatchDetailsAssembler(
        measurementProvider,
        new ProductionCurrentProfileProjector(),
        POTENTIAL_ABV_PREDICTOR,
        vesselReferenceResolver
    );
    return new QueryProductionBatchesService(
        repository,
        accessControl,
        measurementProvider,
        vesselReferenceResolver,
        detailsAssembler
    );
  }
}
