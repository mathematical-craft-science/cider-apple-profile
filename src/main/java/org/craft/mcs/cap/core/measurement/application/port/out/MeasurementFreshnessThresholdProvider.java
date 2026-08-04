package org.craft.mcs.cap.core.measurement.application.port.out;

import java.time.Duration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Supplies the workspace-owned age threshold used to assess measurement evidence freshness. */
public interface MeasurementFreshnessThresholdProvider {

  @NonNull Duration staleAfter(@NonNull WorkspaceId workspaceId);
}
