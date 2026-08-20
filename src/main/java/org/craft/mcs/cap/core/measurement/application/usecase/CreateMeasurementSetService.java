package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.ObservedMaterialStateValidator;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectChangePolicy;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectMeasurementPolicy;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.measurement.application.usecase.MeasurementSetViewMapper.toView;


/** Records a new logical measurement set against the material state that was sampled. */
@Component
@RequiredArgsConstructor
public final class CreateMeasurementSetService implements CreateMeasurementSetUseCase {

  private final @NonNull MeasurementSetRepository repository;
  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;
  private final @NonNull MeasurementSubjectChangePolicy subjectChangePolicy;
  private final @NonNull MeasurementSubjectMeasurementPolicy subjectMeasurementPolicy;
  private final @NonNull MeasurementActorProvider actorProvider;
  private final @NonNull ObservedMaterialStateValidator observedMaterialStateValidator;
  private final @NonNull MeasurementSetRevisionFactory revisionFactory;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull MeasurementSetView create(@NonNull CreateMeasurementSetCommand command) {
    requireNonNull(command, "create measurement set command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var workspaceId = subjectAccessControl.requireWriteAccess(command.subject());
      subjectChangePolicy.requireChangesAllowed(command.subject());
      subjectMeasurementPolicy.validate(command.subject(), command.measurements());
      final var observedStateId = observedMaterialStateValidator.requireObservedState(
          command.subject(),
          command.observedMaterialStateId()
      );
      final var now = clock.instant();
      final var requestedId = command.requestedId();
      final var measurementSetId = requestedId == null
          ? MeasurementSetId.newId()
          : requestedId;
      final var set = MeasurementSet.create(
          measurementSetId,
          workspaceId,
          command.subject(),
          revisionFactory.initial(
              command,
              observedStateId,
              now,
              actorProvider.requireCurrentActorReference()
          )
      );
      return toView(repository.create(set));
    });
  }
}
