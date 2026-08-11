package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Retracts a logical measurement set while preserving its immutable evidence history. */
@Component
@RequiredArgsConstructor
public final class RetractMeasurementSetService implements RetractMeasurementSetUseCase {

  private final @NonNull MeasurementSetRepository repository;
  private final @NonNull MeasurementSetWriteAccess writeAccess;
  private final @NonNull MeasurementSetRevisionFactory revisionFactory;
  private final @NonNull MeasurementActorProvider actorProvider;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public void retract(@NonNull RetractMeasurementSetCommand command) {
    requireNonNull(command, "retract measurement set command cannot be null");
    transactionHelper.executeInTransaction(() -> {
      final var state = writeAccess.require(command.measurementSetId(), command.expectedVersion());
      final var retraction = revisionFactory.retraction(
          state.aggregate().currentRevision(),
          command.reason(),
          clock.instant(),
          actorProvider.requireCurrentActorReference()
      );
      state.aggregate().retract(retraction);
      repository.appendRevision(state);
    });
  }
}
