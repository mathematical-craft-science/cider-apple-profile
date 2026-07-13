package org.craft.mcs.cap.core.account.domain.model;


/** Consent-controlled visibility of contributor contact details. */
public enum ContactVisibility {
  /** Contact data remains private and is never exposed through public contributor views. */
  PRIVATE,

  /** The contributor has explicitly consented to public contact visibility. */
  PUBLIC
}
