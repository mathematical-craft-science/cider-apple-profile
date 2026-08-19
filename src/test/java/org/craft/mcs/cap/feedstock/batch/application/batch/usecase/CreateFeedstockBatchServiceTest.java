package org.craft.mcs.cap.feedstock.batch.application.batch.usecase;

import java.time.LocalDate;
import java.time.Clock;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchTraceRecorder;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase.CreateFeedstockBatchService;
import org.craft.mcs.cap.core.feedstock.batch.application.vessel.port.out.FeedstockVesselAssignmentValidator;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CreateFeedstockBatchServiceTest {

  @Test
  void createsFeedstockBatchInValidatedVessel() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var vesselAssignmentValidator = mock(FeedstockVesselAssignmentValidator.class);
    final var service = new CreateFeedstockBatchService(
        repository,
        mock(FeedstockBatchTraceRecorder.class),
        mock(FeedstockAccessControl.class),
        vesselAssignmentValidator,
        transactionHelper(),
        Clock.systemUTC()
    );
    final var workspaceId = WorkspaceId.of(newUuid());
    final var vesselId = VesselTestFixtures.FEEDSTOCK_VESSEL_ID;
    when(repository.create(any(FeedstockBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(vesselAssignmentValidator.requireAssignable(workspaceId, vesselId, VolumeValue.ofLiters("250.000")))
        .thenReturn(VesselTestFixtures.reference(vesselId, "Tank A-01"));

    final var created = service.create(new CreateFeedstockBatchCommand(
        workspaceId,
        BatchName.of("Late season fruit"),
        vesselId,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        VolumeValue.ofLiters("250.000"),
        "Pressed in two sessions"
    ));

    assertThat(created.batch())
        .satisfies(view -> assertThat(view.batch())
            .returns(workspaceId, FeedstockBatchSummary::workspaceId)
            .returns(vesselId, FeedstockBatchSummary::vesselId)
            .returns(null, FeedstockBatchSummary::source)
            .returns("Pressed in two sessions", FeedstockBatchSummary::note)
            .satisfies(batch -> assertThat(batch.availableVolume().liters())
                .isEqualByComparingTo("250.000")))
        .returns(
            VesselTestFixtures.reference(vesselId, "Tank A-01"),
            FeedstockBatchView::vessel
        );
  }

  @Test
  void rejectsCreatingFeedstockBatchWithZeroAvailableVolume() {
    final var repository = mock(FeedstockBatchRepository.class);
    final var vesselAssignmentValidator = mock(FeedstockVesselAssignmentValidator.class);
    final var service = new CreateFeedstockBatchService(
        repository,
        mock(FeedstockBatchTraceRecorder.class),
        mock(FeedstockAccessControl.class),
        vesselAssignmentValidator,
        transactionHelper(),
        Clock.systemUTC()
    );
    final var command = new CreateFeedstockBatchCommand(
        WorkspaceId.of(newUuid()),
        BatchName.of("Late season fruit"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        null,
        VolumeValue.ofLiters("0"),
        null
    );

    assertThatThrownBy(() -> service.create(command))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active batch material volume must be greater than zero");
    verify(repository, never()).create(any(FeedstockBatch.class));
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @NonNull
      @Override
      public <T> T callInTransactionNonNull(@NonNull Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable action) {
        action.run();
      }
    };
  }
}
