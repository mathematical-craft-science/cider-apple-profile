package org.craft.mcs.cap.core.account.domain.model;


/** Business type of a CAP workspace. */
public enum WorkspaceType {
  /** A cidery or cidermaking production team. */
  CIDERY,

  /** A laboratory contributing or validating analytical measurements. */
  LABORATORY,

  /** A research or breeding institute. */
  INSTITUTE,

  /** A catalog-curation organization. */
  CATALOG,

  /** A workspace whose business type does not match a predefined category. */
  CUSTOM
}
