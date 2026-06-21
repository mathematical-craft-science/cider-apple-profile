package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PredictionConfidence;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProductionBatchRestMapperTest {

  private final PotentialAbvPredictor potentialAbvPredictor = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  @Test
  void mapsYanAndCalculatedPotentialAbvToDetailsResponse() {
    final var batch = productionBatch();

    final var response = ProductionBatchRestMapper.toDetailsResponse(batch, potentialAbvPredictor);

    assertThat(response.yeastAssimilableNitrogen()).isNotNull();
    assertThat(response.yeastAssimilableNitrogen().value()).isEqualByComparingTo("180");
    assertThat(response.yeastAssimilableNitrogen().unit()).isEqualTo("mg N/L");
    assertThat(response.volume().unit()).isEqualTo("L");
    assertThat(response.profile().sugar().value().unit()).isEqualTo("SG");
    assertThat(response.profile().acidity().value().unit()).isEqualTo("g/L as malic acid");
    assertThat(response.profile().tannin()).isNotNull();
    assertThat(response.profile().tannin().value().unit()).isEqualTo("mg/L TAE");
    assertThat(response.potentialAbv().originalGravity().value()).isEqualByComparingTo("1.052");
    assertThat(response.potentialAbv().predictedFinalGravity().value()).isEqualByComparingTo("1.000");
    assertThat(response.potentialAbv().predictedFinalGravitySource())
        .isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT);
    assertThat(response.potentialAbv().potentialAbv().value()).isEqualByComparingTo("6.825");
    assertThat(response.potentialAbv().potentialAbv().unit()).isEqualTo("% ABV");
    assertThat(response.potentialAbv().method()).isEqualTo(AbvPredictionMethod.STANDARD_GRAVITY_DROP);
    assertThat(response.potentialAbv().confidence()).isEqualTo(PredictionConfidence.LOW);
  }

  @Test
  void mapsYanToCreateResponseComposition() {
    final var response = ProductionBatchRestMapper.toResponse(
        new ProductionBatchCreated(
            productionBatch(),
            new ProductionInput(
                ProductionSourceComposition.of(List.of(
                    new ProductionSourceComponent(
                        ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value()),
                        VolumeValue.ofLiters("120")
                    )
                )),
                profileSnapshot(),
                null
            )
        )
    );

    assertThat(response.composition().yeastAssimilableNitrogen()).isNotNull();
    assertThat(response.composition().yeastAssimilableNitrogen().value()).isEqualByComparingTo("180");
    assertThat(response.composition().yeastAssimilableNitrogen().unit()).isEqualTo("mg N/L");
  }

  @Test
  void mapsYanAndCalculatedPotentialAbvToListResponseItems() {
    final var response = ProductionBatchRestMapper.toListResponse(List.of(productionBatch()), potentialAbvPredictor);

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
        CiderMakerId.of(UUID.randomUUID()),
        ProductionBatchName.of("Late season cuvee"),
        ProductionBatchKind.CIDER,
        VolumeValue.ofLiters("120"),
        profileSnapshot(),
        null,
        null
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
}
