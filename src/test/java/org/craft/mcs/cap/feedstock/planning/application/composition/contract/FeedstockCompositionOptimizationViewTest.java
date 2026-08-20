package org.craft.mcs.cap.feedstock.planning.application.composition.contract;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FeedstockCompositionOptimizationViewTest {

  @Test
  void exposesAbsoluteSourceWeightWithoutAProportion() {
    final var source = new FeedstockCompositionOptimizationView.Source(
        FeedstockBatchId.newId(),
        new FeedstockCompositionOptimizationView.AbsoluteVolume(VolumeValue.ofLiters("100"))
    );

    assertThat(source)
        .returns(VolumeValue.ofLiters("100"), FeedstockCompositionOptimizationView.Source::requestedVolume)
        .returns(null, FeedstockCompositionOptimizationView.Source::proportion);
  }

  @Test
  void exposesProportionalSourceWeightWithoutARequestedVolume() {
    final var source = new FeedstockCompositionOptimizationView.Source(
        FeedstockBatchId.newId(),
        new FeedstockCompositionOptimizationView.Proportion(new BigDecimal("0.5"))
    );

    assertThat(source)
        .returns(null, FeedstockCompositionOptimizationView.Source::requestedVolume)
        .returns(new BigDecimal("0.5"), FeedstockCompositionOptimizationView.Source::proportion);
  }

  @Test
  void rejectsMissingSourceWeight() {
    assertThatNullPointerException()
        .isThrownBy(() -> new FeedstockCompositionOptimizationView.Source(FeedstockBatchId.newId(), null));
  }

  @Test
  void rejectsMissingCalculatedSpecificGravity() {
    assertThatNullPointerException()
        .isThrownBy(() -> new FeedstockCompositionOptimizationView.Characteristics(
            null,
            TitratableAcidityValue.of("6.2"),
            null,
            null
        ));
  }

  @Test
  void rejectsMissingPotentialAbvSource() {
    assertThatNullPointerException()
        .isThrownBy(() -> new FeedstockCompositionOptimizationView.PotentialAbv(
            SpecificGravityValue.of("1.052"),
            SpecificGravityValue.of("1.000"),
            null,
            AbvValue.of(new BigDecimal("6.825")),
            FeedstockCompositionOptimizationView.AbvMethod.STANDARD_GRAVITY_DROP,
            FeedstockCompositionOptimizationView.Confidence.MEDIUM
        ));
  }

  @Test
  void copiesObjectivePenalties() {
    final var penalties = new ArrayList<FeedstockCompositionOptimizationView.Penalty>();
    penalties.add(new FeedstockCompositionOptimizationView.Penalty(
        FeedstockCompositionOptimizationView.PenaltyCode.LOW_YEAST_ASSIMILABLE_NITROGEN,
        FeedstockCompositionOptimizationView.PenaltySeverity.WARNING,
        BigDecimal.ONE,
        null,
        null,
        null
    ));

    final var objective = new FeedstockCompositionOptimizationView.Objective(BigDecimal.ONE, penalties);
    penalties.clear();

    assertThat(objective.penalties()).hasSize(1);
  }

  @Test
  void rejectsMissingPenaltyCode() {
    assertThatNullPointerException()
        .isThrownBy(() -> new FeedstockCompositionOptimizationView.Penalty(
            null,
            FeedstockCompositionOptimizationView.PenaltySeverity.WARNING,
            BigDecimal.ONE,
            null,
            null,
            null
        ));
  }
}
