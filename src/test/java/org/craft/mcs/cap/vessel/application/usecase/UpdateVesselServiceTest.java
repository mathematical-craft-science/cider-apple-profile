package org.craft.mcs.cap.vessel.application.usecase;

import java.util.Optional;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.vessel.application.VesselOccupiedException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.contract.VesselView;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselCommand;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.core.vessel.application.usecase.UpdateVesselService;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class UpdateVesselServiceTest {

  @Test
  void updatesVesselWithExpectedRevision() {
    final var repository = mock(VesselRepository.class);
    final var accessControl = mock(VesselAccessControl.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var state = vesselState();
    when(repository.lockVersionedById(state.aggregate().vesselId())).thenReturn(Optional.of(state));
    when(currentContentQuery.findByVesselId(state.aggregate().vesselId())).thenReturn(Optional.empty());
    when(repository.update(any())).thenAnswer(invocation -> new VersionedAggregate<>(
        invocation.<VersionedAggregate<Vessel>>getArgument(0).aggregate(),
        AggregateRevision.of(3)
    ));
    final var service = new UpdateVesselService(
        repository,
        currentContentQuery,
        accessControl,
        transactionHelper()
    );

    final var view = service.update(new UpdateVesselCommand(
        state.aggregate().vesselId(),
        state.aggregate().workspaceId(),
        AggregateRevision.of(2),
        "Barrel B-12",
        VolumeValue.ofLiters("225"),
        VesselMaterial.WOOD,
        VesselOperationalStatus.OUT_OF_SERVICE
    ));

    assertThat(view.vessel())
        .returns("Barrel B-12", VesselView::name)
        .returns(VolumeValue.ofLiters("225"), VesselView::nominalCapacity)
        .returns(VesselMaterial.WOOD, VesselView::material)
        .returns(VesselOperationalStatus.OUT_OF_SERVICE, VesselView::operationalStatus)
        .returns(AggregateRevision.of(3), VesselView::revision);
    assertThat(view).returns(null, VesselUsageView::currentContent);
    verify(accessControl).requireWriteAccess(state.aggregate().workspaceId());
  }

  @Test
  void rejectsStaleExpectedRevisionBeforeMutation() {
    final var repository = mock(VesselRepository.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var state = vesselState();
    when(repository.lockVersionedById(state.aggregate().vesselId())).thenReturn(Optional.of(state));
    final var service = new UpdateVesselService(
        repository,
        currentContentQuery,
        mock(VesselAccessControl.class),
        transactionHelper()
    );
    final var command = new UpdateVesselCommand(
        state.aggregate().vesselId(),
        state.aggregate().workspaceId(),
        AggregateRevision.of(1),
        "Barrel B-12",
        VolumeValue.ofLiters("225"),
        VesselMaterial.WOOD,
        VesselOperationalStatus.OPERATIONAL
    );

    assertThatThrownBy(() -> service.update(command))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    verify(repository, never()).update(any());
  }

  @Test
  void rejectsCapacityBelowCurrentMaterialVolume() {
    final var repository = mock(VesselRepository.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var state = vesselState();
    when(repository.lockVersionedById(state.aggregate().vesselId())).thenReturn(Optional.of(state));
    when(currentContentQuery.findByVesselId(state.aggregate().vesselId()))
        .thenReturn(Optional.of(currentContent(state.aggregate().vesselId())));
    final var service = new UpdateVesselService(
        repository,
        currentContentQuery,
        mock(VesselAccessControl.class),
        transactionHelper()
    );

    assertThatThrownBy(() -> service.update(command(
        state,
        VolumeValue.ofLiters("99"),
        VesselOperationalStatus.OPERATIONAL
    )))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("material volume exceeds vessel nominal capacity");
    verify(repository, never()).update(any());
  }

  @ParameterizedTest
  @EnumSource(
      value = VesselOperationalStatus.class,
      names = {"OUT_OF_SERVICE", "RETIRED"}
  )
  void rejectsUnavailableStatusWhileVesselContainsActiveMaterial(
      @NonNull VesselOperationalStatus targetStatus
  ) {
    final var repository = mock(VesselRepository.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var state = vesselState();
    when(repository.lockVersionedById(state.aggregate().vesselId())).thenReturn(Optional.of(state));
    when(currentContentQuery.findByVesselId(state.aggregate().vesselId()))
        .thenReturn(Optional.of(currentContent(state.aggregate().vesselId())));
    final var service = new UpdateVesselService(
        repository,
        currentContentQuery,
        mock(VesselAccessControl.class),
        transactionHelper()
    );

    assertThatThrownBy(() -> service.update(command(
        state,
        VolumeValue.ofLiters("500"),
        targetStatus
    )))
        .isInstanceOf(VesselOccupiedException.class)
        .hasMessageContaining("is occupied by FEEDSTOCK_BATCH");
    verify(repository, never()).update(any());
  }

  @Test
  void allowsCatalogUpdateForOccupiedOperationalVesselWithoutReducingCapacityBelowItsContent() {
    final var repository = mock(VesselRepository.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var state = vesselState();
    final var currentContent = currentContent(state.aggregate().vesselId());
    when(repository.lockVersionedById(state.aggregate().vesselId())).thenReturn(Optional.of(state));
    when(currentContentQuery.findByVesselId(state.aggregate().vesselId())).thenReturn(Optional.of(currentContent));
    when(repository.update(any())).thenAnswer(invocation -> new VersionedAggregate<>(
        invocation.<VersionedAggregate<Vessel>>getArgument(0).aggregate(),
        AggregateRevision.of(3)
    ));
    final var service = new UpdateVesselService(
        repository,
        currentContentQuery,
        mock(VesselAccessControl.class),
        transactionHelper()
    );

    final var view = service.update(command(
        state,
        currentContent.volume(),
        VesselOperationalStatus.OPERATIONAL
    ));

    assertThat(view)
        .returns(currentContent, VesselUsageView::currentContent)
        .returns(VolumeValue.ofLiters("100"), VesselUsageView::usedVolume)
        .returns(VolumeValue.ofLiters("0"), VesselUsageView::remainingCapacity);
    assertThat(view.vessel().operationalStatus()).isEqualTo(VesselOperationalStatus.OPERATIONAL);
    verify(repository).update(any());
  }

  private static @NonNull UpdateVesselCommand command(
      @NonNull VersionedAggregate<Vessel> state,
      @NonNull VolumeValue capacity,
      @NonNull VesselOperationalStatus status
  ) {
    return new UpdateVesselCommand(
        state.aggregate().vesselId(),
        state.aggregate().workspaceId(),
        state.revision(),
        state.aggregate().name(),
        capacity,
        state.aggregate().material(),
        status
    );
  }

  private static @NonNull VesselCurrentContentView currentContent(@NonNull VesselId vesselId) {
    return new VesselCurrentContentView(
        vesselId,
        VesselCurrentContentType.FEEDSTOCK_BATCH,
        newUuid(),
        MaterialStateId.newId(),
        BatchName.of("Existing juice"),
        VolumeValue.ofLiters("100")
    );
  }

  private static @NonNull VersionedAggregate<Vessel> vesselState() {
    return new VersionedAggregate<>(
        Vessel.create(
            VesselId.newId(),
            WorkspaceId.newId(),
            "Tank F-03",
            VolumeValue.ofLiters("500"),
            VesselMaterial.STAINLESS_STEEL
        ),
        AggregateRevision.of(2)
    );
  }

  private static @NonNull TransactionHelper transactionHelper() {
    return new TransactionHelper() {
      @Override
      public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> action) {
        return action.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable action) {
        action.run();
      }
    };
  }
}
