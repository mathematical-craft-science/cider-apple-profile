package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateProvider;
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
  private final @NonNull MeasurementMaterialStateProvider materialStateProvider;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public void retract(@NonNull RetractMeasurementSetCommand command) {
    requireNonNull(command, "retract measurement set command cannot be null");
    transactionHelper.executeInTransaction(() -> {
      final var state = writeAccess.require(command.measurementSetId(), command.expectedVersion());
      materialStateProvider.lockCurrentState(state.aggregate().subject());
      state.aggregate().retract(clock.instant());
      repository.retract(state);
    });
  }
}
