package org.craft.mcs.cap.infrastructure.adapter.out.settings.account;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Adapts Account-owned workspace settings to the generic Measurement freshness port. */
@Component
@RequiredArgsConstructor
public final class AccountMeasurementFreshnessThresholdProvider
    implements MeasurementFreshnessThresholdProvider {

  private final @NonNull WorkspaceSettingsReader workspaceSettingsReader;

  @Override
  public @NonNull Duration staleAfter(@NonNull WorkspaceId workspaceId) {
    return workspaceSettingsReader.readByWorkspaceId(workspaceId).measurementStaleAfter();
  }
}
