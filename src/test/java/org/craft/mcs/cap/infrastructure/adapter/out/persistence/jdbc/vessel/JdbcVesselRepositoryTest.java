package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


class JdbcVesselRepositoryTest {

  @Test
  void updatesUsingCallerRevisionWithoutReadingCurrentRow() {
    final var springDataRepository = mock(SpringDataVesselJdbcRepository.class);
    final var aggregate = Vessel.create(
        VesselId.newId(),
        WorkspaceId.newId(),
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    );
    final var expectedRevision = AggregateRevision.of(3);
    when(springDataRepository.updateByIdAndVersion(
        aggregate.vesselId().value(),
        aggregate.workspaceId().value(),
        aggregate.name(),
        aggregate.nominalCapacity().liters(),
        aggregate.material(),
        aggregate.operationalStatus(),
        expectedRevision.value()
    )).thenReturn(1);
    final var repository = new JdbcVesselRepository(springDataRepository);

    final var updated = repository.update(new VersionedAggregate<>(aggregate, expectedRevision));

    assertThat(updated)
        .returns(aggregate, VersionedAggregate::aggregate)
        .returns(AggregateRevision.of(4), VersionedAggregate::revision);
    verify(springDataRepository, never()).findById(any());
    verify(springDataRepository).updateByIdAndVersion(
        aggregate.vesselId().value(),
        aggregate.workspaceId().value(),
        aggregate.name(),
        aggregate.nominalCapacity().liters(),
        aggregate.material(),
        aggregate.operationalStatus(),
        expectedRevision.value()
    );
    verifyNoMoreInteractions(springDataRepository);
  }
}
