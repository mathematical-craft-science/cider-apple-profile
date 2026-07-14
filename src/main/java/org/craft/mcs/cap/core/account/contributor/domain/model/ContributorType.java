package org.craft.mcs.cap.core.account.contributor.domain.model;

/** Type of catalog or measurement contributor represented by a CAP account. */
public enum ContributorType {
  /** An individual farmer contributing orchard or harvest data. */
  FARMER,

  /** An orchard organization contributing regional source data. */
  ORCHARD,

  /** A producer contributing source-material or production observations. */
  PRODUCER,

  /** A cidermaker contributing practical production observations. */
  CIDERMAKER,

  /** A researcher contributing scientific observations or datasets. */
  RESEARCHER,

  /** A laboratory contributing analytical measurements. */
  LABORATORY,

  /** A catalog curator contributing or validating reference data. */
  CURATOR,

  /** A contributor whose role is not yet classified. */
  UNKNOWN
}
