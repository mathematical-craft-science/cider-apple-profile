package org.craft.mcs.cap.core.measurement.application.port.out;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** ACL boundary for resolving and authorizing a measured Feedstock or Production subject. */
public interface MeasurementSubjectAccessControl {

  @NonNull WorkspaceId requireReadAccess(@NonNull MeasurementSubjectReference subject);

  void requireReadAccess(
      @NonNull MeasurementSubjectType subjectType,
      @NonNull WorkspaceId workspaceId
  );

  @NonNull WorkspaceId requireWriteAccess(@NonNull MeasurementSubjectReference subject);
}
