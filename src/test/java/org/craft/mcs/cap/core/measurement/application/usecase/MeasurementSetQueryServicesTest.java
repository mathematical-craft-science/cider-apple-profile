package org.craft.mcs.cap.core.measurement.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementMaterialStateProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MeasurementSetQueryServicesTest {

  @Test
  void getChecksReadAccessToMeasurementSubject() {
    final var viewQuery = mock(MeasurementSetViewQuery.class);
    final var accessControl = mock(MeasurementSubjectAccessControl.class);
    final var measurementSetId = MeasurementSetId.newId();
    final var subject = subject();
    final var view = mock(MeasurementSetView.class);
    when(view.subject()).thenReturn(subject);
    when(viewQuery.findById(measurementSetId)).thenReturn(Optional.of(view));
    final var service = new QueryMeasurementSetsService(viewQuery, accessControl);

    assertThat(service.get(measurementSetId)).isSameAs(view);
    verify(accessControl).requireReadAccess(subject);
  }

  @Test
  void listScopesQueryToAccessibleWorkspace() {
    final var viewQuery = mock(MeasurementSetViewQuery.class);
    final var accessControl = mock(MeasurementSubjectAccessControl.class);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject();
    final var view = mock(MeasurementSetView.class);
    when(accessControl.requireReadAccess(subject)).thenReturn(workspaceId);
    when(viewQuery.findBySubject(workspaceId, subject)).thenReturn(List.of(view));
    final var service = new QueryMeasurementSetsService(viewQuery, accessControl);

    assertThat(service.list(subject)).containsExactly(view);
  }

  @Test
  void currentStateChecksReadAccessBeforeReadingMaterialState() {
    final var accessControl = mock(MeasurementSubjectAccessControl.class);
    final var materialStateProvider = mock(MeasurementMaterialStateProvider.class);
    final var subject = subject();
    final var materialStateId = MaterialStateId.newId();
    when(materialStateProvider.currentStateOf(subject)).thenReturn(materialStateId);
    final var service = new GetCurrentMeasurementSubjectStateService(
        accessControl,
        materialStateProvider
    );

    assertThat(service.currentStateOf(subject)).isEqualTo(materialStateId);
    verify(accessControl).requireReadAccess(subject);
  }

  private static MeasurementSubjectReference subject() {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid());
  }
}
