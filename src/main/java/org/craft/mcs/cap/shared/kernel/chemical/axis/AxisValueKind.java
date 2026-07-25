package org.craft.mcs.cap.shared.kernel.chemical.axis;

/** Describes how a scalar analysis-axis value was obtained from its source profile. */
public enum AxisValueKind {

  /** One direct point characteristic projected without aggregation. */
  POINT,

  /** Exact midpoint used to project an interval while retaining its lower and upper bounds. */
  INTERVAL_MIDPOINT,

  /** Value calculated from several source characteristics or composition inputs. */
  CALCULATED
}
