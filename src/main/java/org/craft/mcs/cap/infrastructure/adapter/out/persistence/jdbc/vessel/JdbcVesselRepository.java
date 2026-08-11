package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.VesselNameConflictException;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel.entity.VesselJdbcMapper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;


/** Spring Data JDBC aggregate adapter preserving Vessel revisions and uniqueness conflicts. */
@Repository
@RequiredArgsConstructor
class JdbcVesselRepository implements VesselRepository {

  private final @NonNull SpringDataVesselJdbcRepository repository;

  @Override
  public @NonNull VersionedAggregate<Vessel> create(@NonNull Vessel vessel) {
    requireNonNull(vessel, "vessel cannot be null");
    final var saved = translateNameConflict(
        vessel,
        () -> translateOptimisticLocking(
            "vessel",
            () -> repository.save(VesselJdbcMapper.fromDomain(vessel))
        )
    );
    return VesselJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull VersionedAggregate<Vessel> update(@NonNull VersionedAggregate<Vessel> vessel) {
    requireNonNull(vessel, "versioned vessel cannot be null");
    final var aggregate = vessel.aggregate();
    final var affectedRows = translateNameConflict(
        aggregate,
        () -> translateOptimisticLocking(
            "vessel",
            () -> repository.updateByIdAndVersion(
                aggregate.vesselId().value(),
                aggregate.workspaceId().value(),
                aggregate.name(),
                aggregate.nominalCapacity().liters(),
                aggregate.material(),
                aggregate.operationalStatus(),
                vessel.revision().value()
            )
        )
    );
    return complete("vessel", vessel, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<Vessel>> findVersionedById(
      @NonNull VesselId vesselId
  ) {
    requireNonNull(vesselId, "vessel id cannot be null");
    return repository.findById(vesselId.value()).map(VesselJdbcMapper::toVersionedAggregate);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<Vessel>> lockVersionedById(
      @NonNull VesselId vesselId
  ) {
    requireNonNull(vesselId, "vessel id cannot be null");
    return repository.lockById(vesselId.value()).map(VesselJdbcMapper::toVersionedAggregate);
  }

  @Override
  public boolean existsByWorkspaceAndNameExcluding(
      @NonNull WorkspaceId workspaceId,
      @NonNull String name,
      @NonNull VesselId excludedVesselId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "vessel name cannot be null");
    requireNonNull(excludedVesselId, "excluded vessel id cannot be null");
    return repository.existsByWorkspaceIdAndNameIgnoreCaseAndVesselIdNot(
        workspaceId.value(),
        name.strip(),
        excludedVesselId.value()
    );
  }

  private static <T extends @NonNull Object> T translateNameConflict(
      @NonNull Vessel vessel,
      @NonNull Supplier<T> operation
  ) {
    try {
      return operation.get();
    } catch (DuplicateKeyException exception) {
      final var message = exception.getMostSpecificCause().getMessage();
      if (message != null && message.contains("ux_vessels_workspace_lower_name")) { // NOSONAR
        throw new VesselNameConflictException(vessel.workspaceId(), vessel.name());
      }
      throw exception;
    }
  }
}
