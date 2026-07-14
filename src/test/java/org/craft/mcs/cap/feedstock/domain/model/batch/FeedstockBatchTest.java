package org.craft.mcs.cap.feedstock.domain.model.batch;

import java.time.LocalDate;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;


class FeedstockBatchTest {

  @Test
  void createsBatchWithOptionalSourceAndAvailableVolume() {
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );

    assertThat(batch.source()).isNull();

    batch.changeSource(FeedstockSource.of("North orchard"));

    assertThat(batch.source()).isNotNull();
    assertThat(batch.source().value()).isEqualTo("North orchard");
    assertThat(batch.availableVolume().liters()).isEqualByComparingTo("250.000");
  }

  @Test
  void marksBatchAsDeletedWithoutLosingDomainState() {
    final var deletedAt = feedstockMeasurementInstant();
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );

    batch.delete(deletedAt);

    assertThat(batch.deleted()).isTrue();
    assertThat(batch.deletedAt()).isEqualTo(deletedAt);
    assertThat(batch.availableVolume().liters()).isEqualByComparingTo("250.000");
  }

  @Test
  void rejectsConsumingMoreThanAvailableVolumeAsDomainConflict() {
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );

    assertThatThrownBy(() -> batch.consumeAvailableVolume(VolumeValue.ofLiters("251.000")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("exceeds available volume");
  }

  @Test
  void rejectsCreatingBatchWithZeroAvailableVolume() {
    assertThatThrownBy(() -> FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("available volume must be greater than zero");
  }

  @Test
  void rejectsChangingBatchAvailableVolumeToZero() {
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(randomUUID()),
        FeedstockBatchName.of("Late season fruit"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250.000")
    );

    assertThatThrownBy(() -> batch.changeAvailableVolume(VolumeValue.ofLiters("0")))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("available volume must be greater than zero");
  }
}
