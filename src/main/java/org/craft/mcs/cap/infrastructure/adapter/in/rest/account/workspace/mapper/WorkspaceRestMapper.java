package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.mapper;

import java.time.DateTimeException;
import java.time.Duration;
import org.craft.mcs.cap.core.account.administration.application.port.in.CreateWorkspaceCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.UpdateWorkspaceCommand;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.AccountWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.CreateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.UpdateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.UpdateWorkspaceSettingsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.WorkspaceSettingsResponse;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Maps workspace lifecycle and settings contracts. */
public final class WorkspaceRestMapper {

  private WorkspaceRestMapper() {
  }

  public static @NonNull AccountWorkspaceResponse toResponse(@NonNull WorkspaceView view) {
    return new AccountWorkspaceResponse(
        view.workspaceId().value(),
        view.name().value(),
        view.type(),
        view.status()
    );
  }

  public static @NonNull WorkspaceSettingsResponse toResponse(@NonNull WorkspaceSettingsView view) {
    return new WorkspaceSettingsResponse(
        view.workspaceId().value(),
        view.measurementStaleAfter().toString(),
        view.defaultAcidityBasis(),
        view.revision().value(),
        view.createdAt(),
        view.updatedAt()
    );
  }

  public static @NonNull CreateWorkspaceCommand toCommand(@NonNull CreateWorkspaceRequest request) {
    requireNonNull(request, "create workspace request cannot be null");
    return new CreateWorkspaceCommand(DisplayName.of(request.name()), request.type());
  }

  public static @NonNull UpdateWorkspaceCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull UpdateWorkspaceRequest request
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(request, "update workspace request cannot be null");
    return new UpdateWorkspaceCommand(workspaceId, DisplayName.of(request.name()), request.type());
  }

  public static @NonNull UpdateWorkspaceSettingsCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull UpdateWorkspaceSettingsRequest request
  ) {
    return new UpdateWorkspaceSettingsCommand(
        workspaceId,
        WorkspaceSettingsRevision.of(request.expectedRevision()),
        parseDuration(request.measurementStaleAfter()),
        request.defaultAcidityBasis()
    );
  }

  private static @Nullable Duration parseDuration(@Nullable String value) {
    if (value == null) {
      return null;
    }
    try {
      return Duration.parse(value);
    } catch (DateTimeException _) {
      throw new ApplicationValidationException(
          "measurementStaleAfter must be a valid ISO-8601 duration"
      );
    }
  }
}
