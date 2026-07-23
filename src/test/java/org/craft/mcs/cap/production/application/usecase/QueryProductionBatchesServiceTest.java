package org.craft.mcs.cap.production.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.application.usecase.QueryProductionBatchesService;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    final var service = new QueryProductionBatchesService(repository, accessControl, POTENTIAL_ABV_PREDICTOR);
    when(repository.findById(batch.batchId())).thenReturn(Optional.of(batch));

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
    final var service = new QueryProductionBatchesService(repository, accessControl, POTENTIAL_ABV_PREDICTOR);
    when(repository.findAllByWorkspaceId(batch.workspaceId())).thenReturn(List.of(batch));

    final var result = service.list(new ListProductionBatchesQuery(batch.workspaceId()));

    assertThat(result)
        .singleElement()
        .satisfies(details -> assertThat(details.batch())
            .returns(batch.batchId(), ProductionBatchView::batchId)
            .returns(batch.workspaceId(), ProductionBatchView::workspaceId));
    verify(accessControl).requireReadAccess(batch.workspaceId());
  }

  private static ProductionBatchView productionBatch() {
    return new ProductionBatchView(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Late season cider"),
        Vessel.named("Tank F-03"),
        ProductionBatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        VolumeValue.ofLiters("120"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("6.2"),
            null,
            null
        ),
        null
    );
  }
}
