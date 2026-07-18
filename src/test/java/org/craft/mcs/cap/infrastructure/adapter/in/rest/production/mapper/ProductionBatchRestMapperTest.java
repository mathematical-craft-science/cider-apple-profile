package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PredictionConfidence;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;


class ProductionBatchRestMapperTest {

  private final PotentialAbvPredictor potentialAbvPredictor = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  @Test
  void mapsYanAndCalculatedPotentialAbvToDetailsResponse() {
    final var batch = productionBatch();
    final var view = productionBatchView(batch);

    final var response = ProductionBatchRestMapper.toDetailsResponse(
        new ProductionBatchDetailsView(
            view,
            potentialAbvPredictor.predict(view.profile().sugar())
        )
    );

    assertThat(response)
        .satisfies(details -> assertThat(details.yeastAssimilableNitrogen())
            .isNotNull()
            .returns("mg N/L", DecimalQuantityResponse::unit)
            .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo("180")))
        .satisfies(details -> assertThat(details.volume().unit()).isEqualTo("L"))
        .satisfies(details -> assertThat(details.profile().sugar().value().unit()).isEqualTo("SG"))
        .satisfies(details -> assertThat(details.profile().acidity().value().unit())
            .isEqualTo("g/L as malic acid"))
        .satisfies(details -> assertThat(details.profile().tannin())
            .isNotNull()
            .satisfies(tannin -> assertThat(tannin.value().unit()).isEqualTo("mg/L TAE")))
        .satisfies(details -> assertThat(details.potentialAbv().originalGravity().value())
            .isEqualByComparingTo("1.052"))
        .satisfies(details -> assertThat(details.potentialAbv().predictedFinalGravity().value())
            .isEqualByComparingTo("1.000"))
        .satisfies(details -> assertThat(details.potentialAbv().predictedFinalGravitySource())
            .isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT))
        .satisfies(details -> assertThat(details.potentialAbv().potentialAbv().value())
            .isEqualByComparingTo("6.825"))
        .satisfies(details -> assertThat(details.potentialAbv().potentialAbv().unit()).isEqualTo("% ABV"))
        .satisfies(details -> assertThat(details.potentialAbv().method())
            .isEqualTo(AbvPredictionMethod.STANDARD_GRAVITY_DROP))
        .satisfies(details -> assertThat(details.potentialAbv().confidence()).isEqualTo(PredictionConfidence.LOW));
  }

  @Test
  void mapsYanAndAuthoritativePotentialAbvToCreateResponseComposition() {
    final var candidate = productionCandidateView();
    final var response = ProductionBatchRestMapper.toResponse(
        new ProductionBatchCreated(
            productionBatchView(productionBatch()),
            candidate
        )
    );

    assertThat(response)
        .returns(candidate.candidateId().value(), ProductionBatchResponse::candidateId)
        .satisfies(result -> assertThat(result.composition().yeastAssimilableNitrogen())
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
            potentialAbvPredictor.predict(view.profile().sugar())
        ))
    );

    assertThat(response.batches()).hasSize(1);
    final var item = response.batches().getFirst();
    assertThat(item.volume().unit()).isEqualTo("L");
    assertThat(item.yeastAssimilableNitrogen()).isNotNull();
    assertThat(item.yeastAssimilableNitrogen().value()).isEqualByComparingTo("180");
    assertThat(item.yeastAssimilableNitrogen().unit()).isEqualTo("mg N/L");
    assertThat(item.potentialAbv().potentialAbv().value()).isEqualByComparingTo("6.825");
    assertThat(item.potentialAbv().potentialAbv().unit()).isEqualTo("% ABV");
  }

  private static ProductionBatch productionBatch() {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        ProductionBatchName.of("Late season cuvee"),
        ProductionBatchKind.CIDER,
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
        batch.kind(),
        batch.status(),
        batch.volume(),
        batch.profile(),
        batch.note()
    );
  }

  private static ProductionProfileSnapshot profileSnapshot() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        TanninityValue.of("1200"),
        YeastAssimilableNitrogenValue.of("180")
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
        prediction(profileSnapshot().sugar()),
        null,
        null,
        List.of(),
        ProductionBatchId.newId()
    );
  }
}
