package org.craft.mcs.cap.feedstock.batch.application.inventory.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchMaterialSnapshotChangedException;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceExpectation;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ValidateFeedstockBatchMaterialSnapshotCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase.ValidateFeedstockBatchMaterialSnapshotService;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ValidateFeedstockBatchMaterialSnapshotServiceTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-08-19T10:00:00Z");

  @Test
  void acceptsUnchangedSnapshotUnderAggregateLock() {
    final var fixture = fixture(AggregateRevision.of(4));

    assertThatCode(() -> fixture.service().validate(fixture.command(AggregateRevision.of(4))))
        .doesNotThrowAnyException();
    verify(fixture.repository()).lockVersionedById(fixture.batch().batchId());
  }

  @Test
  void rejectsStaleAggregateRevision() {
    final var fixture = fixture(AggregateRevision.of(5));

    assertThatThrownBy(() -> fixture.service().validate(fixture.command(AggregateRevision.of(4))))
        .isInstanceOf(FeedstockBatchMaterialSnapshotChangedException.class);
  }

  @Test
  void reportsStaleSnapshotBeforeInsufficientVolume() {
    final var fixture = fixture(AggregateRevision.of(5));

    assertThatThrownBy(() -> fixture.service().validate(fixture.command(
        AggregateRevision.of(4),
        VolumeValue.ofLiters("251")
    )))
        .isInstanceOf(FeedstockBatchMaterialSnapshotChangedException.class);
  }

  private static @NonNull Fixture fixture(@NonNull AggregateRevision currentRevision) {
    final var workspaceId = WorkspaceId.newId();
    final var batch = FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late juice"),
        VesselId.newId(),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-11-30")),
        VolumeValue.ofLiters("250")
    );
    final var repository = mock(FeedstockBatchRepository.class);
    when(repository.lockVersionedById(batch.batchId()))
        .thenReturn(Optional.of(new VersionedAggregate<>(batch, currentRevision)));
    final var evidence = evidence(batch.currentMaterialStateId());
    final var profile = mock(FeedstockCurrentProfileView.class);
    when(profile.evidence()).thenReturn(List.of(evidence));
    final var profileQuery = mock(FeedstockCurrentProfileQueryService.class);
    when(profileQuery.requireCurrentProfile(batch.batchId())).thenReturn(profile);
    final var service = new ValidateFeedstockBatchMaterialSnapshotService(
        repository,
        profileQuery,
        mock(FeedstockAccessControl.class),
        new DirectTransactionHelper()
    );
    return new Fixture(
        service,
        repository,
        batch,
        workspaceId,
        FeedstockBatchMaterialEvidenceReference.from(List.of(evidence.snapshot()))
    );
  }

  private static @NonNull MeasurementEvidenceView evidence(@NonNull MaterialStateId materialStateId) {
    return new MeasurementEvidenceView(
        new MeasurementEvidenceSnapshot(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            MeasurementValueOrigin.MEASURED,
            materialStateId,
            new MeasurementEvidenceReference(FeedstockBatchId.newId().value(), FeedstockBatchId.newId().value()),
            MEASURED_AT,
            new MeasurementProvenanceView(MeasurementSourceType.USER, null, null, null, null)
        ),
        MeasurementFreshnessStatus.CURRENT
    );
  }

  private record Fixture(
      @NonNull ValidateFeedstockBatchMaterialSnapshotService service,
      @NonNull FeedstockBatchRepository repository,
      @NonNull FeedstockBatch batch,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockBatchMaterialEvidenceReference evidenceReference
  ) {

    @NonNull ValidateFeedstockBatchMaterialSnapshotCommand command(@NonNull AggregateRevision revision) {
      return command(revision, VolumeValue.ofLiters("100"));
    }

    @NonNull ValidateFeedstockBatchMaterialSnapshotCommand command(
        @NonNull AggregateRevision revision,
        @NonNull VolumeValue requestedVolume
    ) {
      return new ValidateFeedstockBatchMaterialSnapshotCommand(
          workspaceId,
          batch.batchId(),
          requestedVolume,
          revision,
          new FeedstockBatchMaterialEvidenceExpectation(
              evidenceReference,
              batch.currentMaterialStateId()
          )
      );
    }
  }

  private static final class DirectTransactionHelper implements TransactionHelper {

    @Override
    public @NonNull <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
      return supplier.get();
    }

    @Override
    public void executeInTransaction(@NonNull Runnable runnable) {
      runnable.run();
    }
  }
}
