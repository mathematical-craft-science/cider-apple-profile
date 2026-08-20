package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.planning.application.confirmation.contract.ProductionBatchConfirmationResult;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.batch.application.port.in.UpdateProductionBatchCommand;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCurrentProfileView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCurrentCharacteristicView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.UpdateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.productionCandidatePrediction;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.productionPrediction;


class ProductionBatchRestMapperTest {

  @Test
  void mapsYanAndCalculatedPotentialAbvToDetailsResponse() {
    final var batch = productionBatch();
    final var view = productionBatchView(batch);

    final var response = ProductionBatchRestMapper.toDetailsResponse(
        new ProductionBatchDetailsView(
            view,
            vesselReference(view),
            currentProfile(view),
            productionPrediction(view.initialProfileSnapshot().specificGravity())
        )
    );

    assertThat(response)
        .satisfies(details -> assertThat(details.currentProfile().yeastAssimilableNitrogen())
            .isNotNull()
            .satisfies(characteristic -> assertThat(characteristic.value().value())
                .returns("mg N/L", DecimalQuantityResponse::unit)
                .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo("180"))))
        .satisfies(details -> assertThat(details.currentVolume().unit()).isEqualTo("L"))
        .satisfies(details -> assertThat(details.initialProfileSnapshot().specificGravity().unit())
            .isEqualTo("SG"))
        .satisfies(details -> assertThat(details.initialProfileSnapshot().titratableAcidity().unit())
            .isEqualTo("g/L as malic acid"))
        .satisfies(details -> assertThat(details.initialProfileSnapshot().tannin())
            .isNotNull()
            .returns("mg/L TAE", DecimalQuantityResponse::unit))
        .satisfies(details -> assertThat(details.potentialAbv().originalGravity().value())
            .isEqualByComparingTo("1.052"))
        .satisfies(details -> assertThat(details.potentialAbv().predictedFinalGravity().value())
            .isEqualByComparingTo("1.000"))
        .satisfies(details -> assertThat(details.potentialAbv().predictedFinalGravitySource())
            .isEqualTo(PotentialAbvPredictionResponse.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT))
        .satisfies(details -> assertThat(details.potentialAbv().potentialAbv().value())
            .isEqualByComparingTo("6.825"))
        .satisfies(details -> assertThat(details.potentialAbv().potentialAbv().unit()).isEqualTo("% ABV"))
        .satisfies(details -> assertThat(details.potentialAbv().method())
            .isEqualTo(PotentialAbvPredictionResponse.Method.STANDARD_GRAVITY_DROP))
        .satisfies(details -> assertThat(details.potentialAbv().confidence())
            .isEqualTo(PotentialAbvPredictionResponse.Confidence.LOW));
  }

  @Test
  void mapsProductionUpdateRequestToTypedCommand() {
    final var batchId = ProductionBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var vesselId = VesselTestFixtures.vesselId("Tank F-04");

    final var command = ProductionBatchRestMapper.toCommand(
        batchId,
        new UpdateProductionBatchRequest(
            workspaceId.value(),
            "Corrected cider",
            vesselId.value(),
            "Corrected initial placement"
        )
    );

    assertThat(command)
        .returns(batchId, UpdateProductionBatchCommand::batchId)
        .returns(workspaceId, UpdateProductionBatchCommand::workspaceId)
        .returns(BatchName.of("Corrected cider"), UpdateProductionBatchCommand::name)
        .returns(vesselId, UpdateProductionBatchCommand::vesselId)
        .returns("Corrected initial placement", UpdateProductionBatchCommand::note);
  }

  @Test
  void mapsYanAndAuthoritativePotentialAbvToCreateResponseComposition() {
    final var candidate = productionCandidateView();
    final var response = ProductionBatchRestMapper.toResponse(
        new ProductionBatchCreated(
            confirmationResult(productionBatchView(productionBatch())),
            candidate
        )
    );

    assertThat(response)
        .returns(candidate.candidateId().value(), ProductionBatchResponse::candidateId)
        .satisfies(result -> assertThat(
            result.composition().characteristics().yeastAssimilableNitrogen()
        )
            .isNotNull()
            .returns("mg N/L", DecimalQuantityResponse::unit)
            .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo("180")))
        .satisfies(result -> assertThat(result.composition().warnings()).isEmpty())
        .satisfies(result -> assertThat(result.composition().potentialAbv())
            .isNotNull()
            .satisfies(potentialAbv -> assertThat(potentialAbv.potentialAbv().value())
                .isEqualByComparingTo("6.825")))
        .satisfies(result -> assertThat(result.composition().targetDistance()).isNull());
  }

  @Test
  void mapsYanAndCalculatedPotentialAbvToListResponseItems() {
    final var view = productionBatchView(productionBatch());
    final var response = ProductionBatchRestMapper.toListResponse(
        List.of(new ProductionBatchDetailsView(
            view,
            vesselReference(view),
            currentProfile(view),
              productionPrediction(view.initialProfileSnapshot().specificGravity())
        ))
    );

    assertThat(response.batches())
        .singleElement()
        .satisfies(item -> assertThat(item.currentVolume().unit()).isEqualTo("L"))
        .satisfies(item -> assertThat(item.currentProfile().yeastAssimilableNitrogen())
            .isNotNull()
            .satisfies(characteristic -> assertThat(characteristic.value().value())
                .returns("mg N/L", DecimalQuantityResponse::unit)
                .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo("180"))))
        .satisfies(item -> assertThat(item.potentialAbv().potentialAbv())
            .returns("% ABV", DecimalQuantityResponse::unit)
            .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo("6.825")));
  }

  private static ProductionBatch productionBatch() {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Late season cuvee"),
        VesselTestFixtures.vesselId("Tank F-03"),
        VolumeValue.ofLiters("120"),
        profileSnapshot(),
        null
    );
  }

  private static ProductionBatchView productionBatchView(ProductionBatch batch) {
    return new ProductionBatchView(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        batch.vesselId(),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.currentVolume(),
        batch.currentMaterialStateId(),
        batch.initialProfileSnapshot(),
        batch.note()
    );
  }

  private static VesselReferenceView vesselReference(ProductionBatchView batch) {
    return VesselTestFixtures.reference(batch.vesselId(), "Tank F-03");
  }

  private static ProductionBatchConfirmationResult confirmationResult(ProductionBatchView batch) {
    return new ProductionBatchConfirmationResult(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        vesselReference(batch),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.note()
    );
  }

  private static ProductionProfileSnapshot profileSnapshot() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        TanninValue.of("1200"),
        YeastAssimilableNitrogenValue.of("180")
    );
  }

  private static ProductionCurrentProfileView currentProfile(ProductionBatchView batch) {
    final var snapshot = batch.initialProfileSnapshot();
    return new ProductionCurrentProfileView(
        ProductionCurrentCharacteristicView.fromInitialSnapshot(
            PointCharacteristicValue.of(snapshot.specificGravity())
        ),
        ProductionCurrentCharacteristicView.fromInitialSnapshot(
            PointCharacteristicValue.of(snapshot.titratableAcidity())
        ),
        null,
        ProductionCurrentCharacteristicView.fromInitialSnapshot(
            PointCharacteristicValue.of(snapshot.tannin())
        ),
        ProductionCurrentCharacteristicView.fromInitialSnapshot(
            PointCharacteristicValue.of(snapshot.yeastAssimilableNitrogen())
        )
    );
  }

  private static ProductionBatchCandidateView productionCandidateView() {
    return new ProductionBatchCandidateView(
        ProductionBatchCandidateId.newId(),
        WorkspaceId.newId(),
        ProductionBatchCandidateStatus.CONFIRMED,
        ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
            ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value()),
            VolumeValue.ofLiters("120")
        ))),
        profileSnapshot(),
        productionCandidatePrediction(profileSnapshot().specificGravity()),
        null,
        null,
        List.of(),
        ProductionBatchId.newId()
    );
  }
}
