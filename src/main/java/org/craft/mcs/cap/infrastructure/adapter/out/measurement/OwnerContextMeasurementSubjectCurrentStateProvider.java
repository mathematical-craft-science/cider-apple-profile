package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.QueryFeedstockMaterialStatesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectCurrentStateProvider;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.batch.application.port.in.QueryProductionMaterialStatesUseCase;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL adapter that resolves generic measurement subjects through their owning contexts. */
@Component
@RequiredArgsConstructor
final class OwnerContextMeasurementSubjectCurrentStateProvider
    implements MeasurementSubjectCurrentStateProvider {

  private final @NonNull QueryFeedstockMaterialStatesUseCase feedstockMaterialStates;
  private final @NonNull QueryProductionMaterialStatesUseCase productionMaterialStates;

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(subjects, "measurement subjects cannot be null");
    final Map<MeasurementSubjectReference, MaterialStateId> states = new LinkedHashMap<>();
    addFeedstockStates(workspaceId, subjects, states);
    addProductionStates(workspaceId, subjects, states);
    if (states.size() != subjects.size()) {
      throw new ApplicationValidationException(
          "every measurement subject must resolve to one current material state"
      );
    }
    return Map.copyOf(states);
  }

  private void addFeedstockStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects,
      @NonNull Map<MeasurementSubjectReference, MaterialStateId> states
  ) {
    final var feedstockSubjects = subjectsOfType(subjects, MeasurementSubjectType.FEEDSTOCK_BATCH);
    if (feedstockSubjects.isEmpty()) {
      return;
    }
    feedstockMaterialStates.findCurrentStates(
        workspaceId,
        feedstockSubjects.stream()
            .map(subject -> FeedstockBatchId.of(subject.id()))
            .collect(Collectors.toUnmodifiableSet())
    ).forEach((batchId, stateId) -> states.put(
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, batchId.value()),
        stateId
    ));
  }

  private void addProductionStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects,
      @NonNull Map<MeasurementSubjectReference, MaterialStateId> states
  ) {
    final var productionSubjects = subjectsOfType(subjects, MeasurementSubjectType.PRODUCTION_BATCH);
    if (productionSubjects.isEmpty()) {
      return;
    }
    productionMaterialStates.findCurrentStates(
        workspaceId,
        productionSubjects.stream()
            .map(subject -> ProductionBatchId.of(subject.id()))
            .collect(Collectors.toUnmodifiableSet())
    ).forEach((batchId, stateId) -> states.put(
        new MeasurementSubjectReference(MeasurementSubjectType.PRODUCTION_BATCH, batchId.value()),
        stateId
    ));
  }

  private static @NonNull Set<@NonNull MeasurementSubjectReference> subjectsOfType(
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects,
      @NonNull MeasurementSubjectType subjectType
  ) {
    return subjects.stream()
        .filter(subject -> subject.type() == subjectType)
        .collect(Collectors.toUnmodifiableSet());
  }
}
