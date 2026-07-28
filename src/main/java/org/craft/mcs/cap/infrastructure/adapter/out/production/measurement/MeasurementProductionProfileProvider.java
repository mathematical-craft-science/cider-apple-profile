package org.craft.mcs.cap.infrastructure.adapter.out.production.measurement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.compatibility.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.compatibility.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Anti-corruption adapter from generic Measurement projections to Production-owned read models. */
@Component
@RequiredArgsConstructor
final class MeasurementProductionProfileProvider implements ProductionMeasurementProfileProvider {

  private final @NonNull ProjectCurrentMeasurementProfileUseCase measurementProfileProjector;

  @Override
  public @NonNull Optional<@NonNull ProductionMeasuredProfileView> findCurrent(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");
    return measurementProfileProjector.project(subject(batchId))
        .map(MeasurementProductionProfileProvider::toProductionProfile);
  }

  @Override
  public @NonNull Map<@NonNull ProductionBatchId, @NonNull ProductionMeasuredProfileView> findAllCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "production workspace id cannot be null");
    requireNonNull(batchIds, "production batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return Map.of();
    }
    final var subjects = batchIds.stream()
        .map(MeasurementProductionProfileProvider::subject)
        .collect(Collectors.toUnmodifiableSet());
    return measurementProfileProjector.projectAll(workspaceId, subjects)
        .entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> ProductionBatchId.of(entry.getKey().id()),
            entry -> toProductionProfile(entry.getValue())
        ));
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull ProductionBatchId batchId) {
    return new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        batchId.value()
    );
  }

  private static @NonNull ProductionMeasuredProfileView toProductionProfile(
      @NonNull CurrentMeasurementProfileView profile
  ) {
    return new ProductionMeasuredProfileView(
        profile.chemicalProfile(),
        profile.evidence()
    );
  }
}
