package org.craft.mcs.cap.core.measurement.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetMetadataQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
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
  void measurementSetMetadataQueryRemainsWorkspaceScopedAndSetBased() {
    final var metadataQuery = mock(MeasurementSetMetadataQuery.class);
    final var workspaceId = WorkspaceId.newId();
    final var measurementSetId = MeasurementSetId.newId();
    final var metadata = mock(MeasurementSetMetadataView.class);
    when(metadataQuery.findAll(workspaceId, Set.of(measurementSetId)))
        .thenReturn(Map.of(measurementSetId, metadata));
    final var service = new QueryMeasurementSetMetadataService(metadataQuery);

    assertThat(service.findAll(workspaceId, Set.of(measurementSetId)))
        .containsExactlyEntriesOf(Map.of(measurementSetId, metadata));
  }

  private static MeasurementSubjectReference subject() {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid());
  }
}
