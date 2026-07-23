package org.craft.mcs.cap.shared.infrastructure.http;

/** HTTP header names used to carry CAP's stateless workspace context. */
public final class ActiveWorkspaceHttpHeaders {

  public static final String ACTIVE_WORKSPACE_ID = "X-CAP-Workspace-Id";

  private ActiveWorkspaceHttpHeaders() {
  }
}
