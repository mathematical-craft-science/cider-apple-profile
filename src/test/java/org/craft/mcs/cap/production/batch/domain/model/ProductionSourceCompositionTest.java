package org.craft.mcs.cap.production.batch.domain.model;

import java.util.List;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class ProductionSourceCompositionTest {

  @Test
  void derivesTotalVolumeFromUniqueSources() {
    final var composition = ProductionSourceComposition.of(List.of(
        component(reference(), "40"),
        component(reference(), "60")
    ));

    assertThat(composition.totalVolume().liters())
        .isEqualByComparingTo("100");
  }

  @Test
  void rejectsDuplicateSourceReferences() {
    final var sourceReference = ProductionSourceReference.feedstockBatch(
        newUuid()
    );

    assertThatThrownBy(() -> ProductionSourceComposition.of(List.of(
        component(sourceReference, "40"),
        component(sourceReference, "60")
    )))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("each source exactly once");
  }

  @Test
  void requiresAtLeastOneSource() {
    assertThatThrownBy(() -> ProductionSourceComposition.of(List.of()))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source composition must contain at least one source");
  }

  @Test
  void rejectsZeroSourceVolume() {
    assertThatThrownBy(() -> component(reference(), "0"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source volume must be positive");
  }

  private static @NonNull ProductionSourceReference reference() {
    return ProductionSourceReference.feedstockBatch(newUuid());
  }

  private static @NonNull ProductionSourceComponent component(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull String volume
  ) {
    return new ProductionSourceComponent(
        sourceReference,
        VolumeValue.ofLiters(volume)
    );
  }
}
