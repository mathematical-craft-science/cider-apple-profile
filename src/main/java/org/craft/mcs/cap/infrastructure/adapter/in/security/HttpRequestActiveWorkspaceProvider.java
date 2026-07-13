package org.craft.mcs.cap.infrastructure.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.out.ActiveWorkspaceProvider;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.infrastructure.adapter.in.http.ActiveWorkspaceHttpHeaders.ACTIVE_WORKSPACE_ID;


/** Reads and validates the explicit active workspace from the current HTTP request. */
@Component
@RequiredArgsConstructor
public final class HttpRequestActiveWorkspaceProvider implements ActiveWorkspaceProvider {

  private final @NonNull HttpServletRequest request;

  @Override
  public @NonNull Optional<@NonNull WorkspaceId> selectedWorkspaceId() {
    final var headerValue = request.getHeader(ACTIVE_WORKSPACE_ID);
    if (headerValue == null || headerValue.isBlank()) {
      return Optional.empty();
    }

    try {
      return Optional.of(WorkspaceId.of(UUID.fromString(headerValue.strip())));
    } catch (IllegalArgumentException _) {
      throw new ApplicationValidationException("active workspace identifier must be a valid UUID");
    }
  }
}
