package org.craft.mcs.cap.vessel.application.usecase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.vessel.application.VesselNotFoundException;
import org.craft.mcs.cap.core.vessel.application.VesselOwnershipMismatchException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageClassification;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ResolveVesselReferencesQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselAvailabilityQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCatalogQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.craft.mcs.cap.core.vessel.application.usecase.GetVesselService;
import org.craft.mcs.cap.core.vessel.application.usecase.ListAvailableVesselsService;
import org.craft.mcs.cap.core.vessel.application.usecase.ListVesselsService;
import org.craft.mcs.cap.core.vessel.application.usecase.ResolveVesselReferencesService;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class QueryVesselServicesTest {

  @Test
  void returnsOwnedVesselAndRequiresWorkspaceReadAccess() {
    final var catalogQuery = mock(VesselCatalogQuery.class);
    final var accessControl = mock(VesselAccessControl.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var view = vesselView(WorkspaceId.newId(), VesselId.newId());
    final var currentContent = currentContent(view.vesselId());
    when(catalogQuery.findById(view.vesselId())).thenReturn(Optional.of(view));
    when(currentContentQuery.findByVesselId(view.vesselId())).thenReturn(Optional.of(currentContent));
    final var service = new GetVesselService(catalogQuery, currentContentQuery, accessControl);

    final var result = service.get(new GetVesselQuery(view.vesselId(), view.workspaceId()));

    assertThat(result)
        .returns(view, VesselUsageView::vessel)
        .returns(currentContent, VesselUsageView::currentContent)
        .returns(VolumeValue.ofLiters("125"), VesselUsageView::usedVolume)
        .returns(VolumeValue.ofLiters("375"), VesselUsageView::remainingCapacity)
        .returns(VesselUsageClassification.PARTIALLY_FILLED, VesselUsageView::usage);
    verify(accessControl).requireReadAccess(view.workspaceId());
  }

  @Test
  void distinguishesMissingVesselFromWorkspaceOwnershipMismatch() {
    final var catalogQuery = mock(VesselCatalogQuery.class);
    final var view = vesselView(WorkspaceId.newId(), VesselId.newId());
    final var requestedWorkspaceId = WorkspaceId.newId();
    when(catalogQuery.findById(view.vesselId())).thenReturn(Optional.of(view));
    final var service = new GetVesselService(
        catalogQuery,
        mock(VesselCurrentContentQuery.class),
        mock(VesselAccessControl.class)
    );

    assertThatThrownBy(() -> service.get(new GetVesselQuery(view.vesselId(), requestedWorkspaceId)))
        .isInstanceOf(VesselOwnershipMismatchException.class);

    final var missingVesselId = VesselId.newId();
    when(catalogQuery.findById(missingVesselId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(new GetVesselQuery(missingVesselId, requestedWorkspaceId)))
        .isInstanceOf(VesselNotFoundException.class);
  }

  @Test
  void returnsImmutableCatalogAndReferenceCollections() {
    final var workspaceId = WorkspaceId.newId();
    final var view = vesselView(workspaceId, VesselId.newId());
    final var reference = new VesselReferenceView(view.vesselId(), view.name());
    final var catalogQuery = mock(VesselCatalogQuery.class);
    when(catalogQuery.findAllByWorkspace(workspaceId)).thenReturn(new ArrayList<>(List.of(view)));
    when(catalogQuery.findReferences(workspaceId, Set.of(view.vesselId())))
        .thenReturn(new LinkedHashMap<>(Map.of(view.vesselId(), reference)));
    final var accessControl = mock(VesselAccessControl.class);
    final var currentContentQuery = mock(VesselCurrentContentQuery.class);
    final var currentContent = currentContent(view.vesselId());
    when(currentContentQuery.findAllByWorkspace(workspaceId))
        .thenReturn(Map.of(view.vesselId(), currentContent));

    final var vessels = new ListVesselsService(catalogQuery, currentContentQuery, accessControl)
        .list(new ListVesselsQuery(workspaceId));
    final var references = new ResolveVesselReferencesService(catalogQuery, accessControl)
        .resolve(new ResolveVesselReferencesQuery(workspaceId, Set.of(view.vesselId())));

    final var expectedUsage = new VesselUsageView(view, currentContent);
    assertThat(vessels).containsExactly(expectedUsage);
    assertThatThrownBy(() -> vessels.add(expectedUsage)).isInstanceOf(UnsupportedOperationException.class);
    assertThat(references).containsExactly(Map.entry(view.vesselId(), reference));
    assertThatThrownBy(() -> references.put(view.vesselId(), reference))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void returnsAvailableVesselSelectionAndRequiresWorkspaceReadAccess() {
    final var workspaceId = WorkspaceId.newId();
    final var view = vesselView(workspaceId, VesselId.newId());
    final var nameContains = new TextSearchCriteria("tank f");
    final var requiredVolume = VolumeValue.ofLiters("250");
    final var availabilityQuery = mock(VesselAvailabilityQuery.class);
    final var accessControl = mock(VesselAccessControl.class);
    when(availabilityQuery.findAvailable(workspaceId, nameContains, requiredVolume)).thenReturn(List.of(view));
    final var service = new ListAvailableVesselsService(availabilityQuery, accessControl);

    final var vessels = service.listAvailable(new ListAvailableVesselsQuery(
        workspaceId,
        nameContains,
        requiredVolume
    ));

    assertThat(vessels)
        .singleElement()
        .returns(view, VesselUsageView::vessel)
        .returns(null, VesselUsageView::currentContent)
        .returns(VesselUsageClassification.EMPTY, VesselUsageView::usage);
    verify(accessControl).requireReadAccess(workspaceId);
    verify(availabilityQuery).findAvailable(workspaceId, nameContains, requiredVolume);
  }

  @Test
  void rejectsZeroRequiredVolume() {
    final var workspaceId = WorkspaceId.newId();

    assertThatThrownBy(() -> new ListAvailableVesselsQuery(
        workspaceId,
        null,
        VolumeValue.ofLiters("0")
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("required vessel volume must be greater than zero");
  }

  private static VesselView vesselView(
      WorkspaceId workspaceId,
      VesselId vesselId
  ) {
    return new VesselView(
        vesselId,
        workspaceId,
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL,
        VesselOperationalStatus.OPERATIONAL,
        AggregateRevision.of(1)
    );
  }

  private static VesselCurrentContentView currentContent(VesselId vesselId) {
    return new VesselCurrentContentView(
        vesselId,
        VesselCurrentContentType.FEEDSTOCK_BATCH,
        newUuid(),
        MaterialStateId.newId(),
        BatchName.of("October juice"),
        VolumeValue.ofLiters("125")
    );
  }
}
