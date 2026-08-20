package org.craft.mcs.cap.vessel.application.usecase;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.vessel.application.VesselNameConflictException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.contract.VesselView;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselCommand;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselRepository;
import org.craft.mcs.cap.core.vessel.application.usecase.CreateVesselService;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CreateVesselServiceTest {

  @Test
  void createsWorkspaceVesselWithPersistedRevision() {
    final var repository = mock(VesselRepository.class);
    final var accessControl = mock(VesselAccessControl.class);
    final var transactionHelper = transactionHelper();
    final var service = new CreateVesselService(repository, accessControl, transactionHelper);
    final var workspaceId = WorkspaceId.newId();
    when(repository.create(any(Vessel.class))).thenAnswer(invocation -> new VersionedAggregate<>(
        invocation.getArgument(0),
        AggregateRevision.of(1)
    ));

    final var view = service.create(new CreateVesselCommand(
        workspaceId,
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    ));

    assertThat(view.vessel())
        .returns(workspaceId, VesselView::workspaceId)
        .returns("Tank F-03", VesselView::name)
        .returns(VolumeValue.ofLiters("500"), VesselView::nominalCapacity)
        .returns(VesselMaterial.STAINLESS_STEEL, VesselView::material)
        .returns(AggregateRevision.of(1), VesselView::revision);
    assertThat(view)
        .returns(null, VesselUsageView::currentContent)
        .returns(VolumeValue.ofLiters("0"), VesselUsageView::usedVolume)
        .returns(VolumeValue.ofLiters("500"), VesselUsageView::remainingCapacity);
    assertThat(view.vessel().vesselId().value().version()).isEqualTo(7);
    verify(accessControl).requireWriteAccess(workspaceId);
    verify(transactionHelper).callInTransactionNonNull(any());
  }

  @Test
  void rejectsDuplicateWorkspaceNameBeforePersistence() {
    final var repository = mock(VesselRepository.class);
    final var workspaceId = WorkspaceId.newId();
    final var transactionHelper = transactionHelper();
    when(repository.existsByWorkspaceAndNameExcluding(any(), any(), any())).thenReturn(true);
    final var service = new CreateVesselService(
        repository,
        mock(VesselAccessControl.class),
        transactionHelper
    );
    final var command = new CreateVesselCommand(
        workspaceId,
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    );

    assertThatThrownBy(() -> service.create(command))
        .isInstanceOf(VesselNameConflictException.class)
        .hasMessageContaining("already contains vessel named Tank F-03");
    verify(repository, never()).create(any(Vessel.class));
    verify(transactionHelper).callInTransactionNonNull(any());
  }

  private static @NonNull TransactionHelper transactionHelper() {
    final var transactionHelper = mock(TransactionHelper.class);
    when(transactionHelper.callInTransactionNonNull(any()))
        .thenAnswer(invocation -> execute(invocation.getArgument(0)));
    return transactionHelper;
  }

  private static <T extends @NonNull Object> @NonNull T execute(@NonNull Supplier<T> supplier) {
    return requireNonNull(supplier.get());
  }
}
