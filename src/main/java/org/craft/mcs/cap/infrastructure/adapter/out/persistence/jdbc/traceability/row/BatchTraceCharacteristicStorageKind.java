package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row;


/**
 * Persistence representation retained for compatibility with existing trace snapshot rows.
 *
 * <p>Shape and origin remain separate in the trace domain; this enum only encodes them into the
 * existing single database column.</p>
 */
public enum BatchTraceCharacteristicStorageKind {
  POINT,
  INTERVAL_MIDPOINT,
  CALCULATED
}
