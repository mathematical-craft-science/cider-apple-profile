package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

/**
 * Workspace roles assignable through invitation or ordinary role-management commands.
 */
public enum WorkspaceAssignableRoleCode {
  /**
   * Administers a workspace without owner-exclusive lifecycle permissions.
   */
  ADMIN,

  /**
   * Plans and supervises feedstock and production processes.
   */
  TECHNOLOGIST,

  /**
   * Operates feedstock and production processes.
   */
  CIDERMAKER,

  /**
   * Contributes variety catalog data.
   */
  CATALOG_CONTRIBUTOR,

  /**
   * Contributes laboratory measurements and provenance.
   */
  LAB_CONTRIBUTOR,

  /**
   * Reviews and publishes contributed catalog data.
   */
  CURATOR,

  /**
   * Reads workspace product data without changing it.
   */
  VIEWER
}
