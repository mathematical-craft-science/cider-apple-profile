package org.craft.mcs.cap.feedstock.domain.model.batch;

import java.time.LocalDate;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;


class FeedstockBatchTest {

  @Test
  void exposesCreatedBatchIdentityAndMetadata() {
    final var batchId = FeedstockBatchId.newId();
    final var workspaceId = WorkspaceId.of(newUuid());
    final var name = BatchName.of("Late season fruit");
    final var vessel = VesselTestFixtures.vesselId("Tank A-01");
    final var season = FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15"));
    final var batch = FeedstockBatch.create(
        batchId,
        workspaceId,
        name,
        vessel,
        FeedstockType.JUICE,
        season,
        VolumeValue.ofLiters("250.000")
    );

    assertThat(batch)
        .returns(batchId, FeedstockBatch::batchId)
        .returns(workspaceId, FeedstockBatch::workspaceId)
        .returns(name, FeedstockBatch::name)
        .returns(vessel, FeedstockBatch::vesselId)
        .returns(FeedstockType.JUICE, FeedstockBatch::type)
        .returns(season, FeedstockBatch::season)
        .returns(null, FeedstockBatch::source)
        .returns(BatchLifecycle.ACTIVE, FeedstockBatch::lifecycle)
        .satisfies(created -> assertThat(created.availableVolume().liters())
            .isEqualByComparingTo("250.000"))
        .returns(null, FeedstockBatch::note)
        .returns(null, FeedstockBatch::deletedAt)
        .returns(false, FeedstockBatch::deleted);
  }

  @Test
  void changesAndClearsEditableMetadata() {
    final var batch = activeBatch(VolumeValue.ofLiters("250.000"));
    final var renamed = BatchName.of("Pressed late fruit");
    final var changedSeason = FeedstockSeason.of(
        LocalDate.parse("2026-09-01"),
        LocalDate.parse("2026-11-30")
    );
    final var source = FeedstockSource.of("North orchard");

    final var changed = batch
        .rename(renamed)
        .changeType(FeedstockType.BLEND)
        .changeSeason(changedSeason)
        .changeSource(source)
        .changeNote("  Pressed at the orchard  ");

    assertThat(changed)
        .isSameAs(batch)
        .returns(renamed, FeedstockBatch::name)
        .returns(FeedstockType.BLEND, FeedstockBatch::type)
        .returns(changedSeason, FeedstockBatch::season)
        .returns(source, FeedstockBatch::source)
        .returns("Pressed at the orchard", FeedstockBatch::note);

    final var cleared = batch.clearSource().clearNote();

    assertThat(cleared)
        .isSameAs(batch)
        .returns(null, FeedstockBatch::source)
        .returns(null, FeedstockBatch::note);
  }

  @Test
  void treatsBlankNoteAsAbsent() {
    final var batch = activeBatch(VolumeValue.ofLiters("250.000"));

    final var changed = batch.changeNote("   ");

    assertThat(changed)
        .isSameAs(batch)
        .returns(null, FeedstockBatch::note);
  }

  @Test
  void marksDepletedBatchAsDeletedWithoutLosingDomainState() {
    final var deletedAt = feedstockMeasurementInstant();
    final var batch = activeBatch(VolumeValue.ofLiters("250.000"))
        .consumeAvailableVolume(VolumeValue.ofLiters("250.000"));

    final var deleted = batch.delete(deletedAt);

    assertThat(deleted)
        .isSameAs(batch)
        .returns(true, FeedstockBatch::deleted)
        .returns(deletedAt, FeedstockBatch::deletedAt)
        .returns(BatchLifecycle.DEPLETED, FeedstockBatch::lifecycle)
        .satisfies(value -> assertThat(value.availableVolume().liters())
            .isZero());
  }

  @Test
  void rejectsDeletingBatchThatStillContainsActiveMaterial() {
    final var batch = activeBatch(VolumeValue.ofLiters("250.000"));

    assertThatThrownBy(() -> batch.delete(feedstockMeasurementInstant()))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot delete feedstock batch containing active material");
  }

  @Test
  void rejectsDeletingBatchMoreThanOnce() {
    final var batch = activeBatch(VolumeValue.ofLiters("250.000"))
        .consumeAvailableVolume(VolumeValue.ofLiters("250.000"))
        .delete(feedstockMeasurementInstant());

    assertThatThrownBy(() -> batch.delete(feedstockMeasurementInstant().plusSeconds(1)))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("feedstock batch is already deleted");
  }

  @Test
  void rejectsConsumingMoreThanAvailableVolumeAsDomainConflict() {
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
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
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active batch material volume must be greater than zero");
  }

  @ParameterizedTest
  @CsvSource({
      "250.000, 1.000, 249.000, ACTIVE",
      "250.000, 250.000, 0, DEPLETED"
  })
  void consumingVolumeProducesDeclaredLifecycleState(
      String initialVolume,
      String consumedVolume,
      String expectedVolume,
      BatchLifecycle expectedLifecycle
  ) {
    final var batch = activeBatch(VolumeValue.ofLiters(initialVolume));
    final var stateBeforeConsumption = batch.currentMaterialStateId();

    final var consumed = batch.consumeAvailableVolume(VolumeValue.ofLiters(consumedVolume));

    assertThat(consumed)
        .isSameAs(batch)
        .returns(expectedLifecycle, FeedstockBatch::lifecycle)
        .satisfies(updated -> assertThat(updated.currentMaterialStateId())
            .isNotEqualTo(stateBeforeConsumption))
        .satisfies(updated -> assertThat(updated.availableVolume().liters())
            .isEqualByComparingTo(expectedVolume));
  }

  @ParameterizedTest
  @EnumSource(value = BatchLifecycle.class, names = {"DEPLETED", "DISCARDED"})
  void rejectsConsumingMaterialFromInactiveBatch(BatchLifecycle lifecycle) {
    final var batch = FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        WorkspaceId.of(newUuid()),
        BatchName.of("Consumed fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        lifecycle,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId(),
        null,
        null
    );

    assertThatThrownBy(() -> batch.consumeAvailableVolume(VolumeValue.ofLiters("1")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot consume material when batch lifecycle is " + lifecycle);
  }

  @Test
  void rejectsConsumingMaterialFromDeletedBatch() {
    final var batch = FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        WorkspaceId.of(newUuid()),
        BatchName.of("Consumed fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        BatchLifecycle.DEPLETED,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId(),
        null,
        feedstockMeasurementInstant()
    );

    assertThatThrownBy(() -> batch.consumeAvailableVolume(VolumeValue.ofLiters("1")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot consume deleted feedstock batch");
  }

  @Test
  void rejectsActiveLifecycleWithZeroVolumeDuringRestore() {
    assertThatThrownBy(() -> restore(
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active batch material volume must be greater than zero");
  }

  @Test
  void rejectsRestoringDeletedBatchWithActiveMaterial() {
    assertThatThrownBy(() -> FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        WorkspaceId.of(newUuid()),
        BatchName.of("Deleted active fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("250"),
        MaterialStateId.newId(),
        null,
        feedstockMeasurementInstant()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("deleted feedstock batch cannot contain active material");
  }

  @ParameterizedTest
  @EnumSource(value = BatchLifecycle.class, names = {"DEPLETED", "DISCARDED"})
  void rejectsInactiveLifecycleWithRemainingVolumeDuringRestore(BatchLifecycle lifecycle) {
    assertThatThrownBy(() -> restore(
        lifecycle,
        VolumeValue.ofLiters("1")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("inactive batch material volume must be zero");
  }

  private static @NonNull FeedstockBatch activeBatch(@NonNull VolumeValue availableVolume) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        availableVolume
    );
  }

  private static @NonNull FeedstockBatch restore(
      @NonNull BatchLifecycle lifecycle,
      @NonNull VolumeValue availableVolume
  ) {
    return FeedstockBatch.restore(
        FeedstockBatchId.newId(),
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        lifecycle,
        availableVolume,
        MaterialStateId.newId(),
        null,
        null
    );
  }
}
