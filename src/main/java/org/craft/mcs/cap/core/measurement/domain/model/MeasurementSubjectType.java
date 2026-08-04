package org.craft.mcs.cap.core.measurement.domain.model;


/** Aggregate types whose material state can be measured in the first measurement model version. */
public enum MeasurementSubjectType {

  /** Source material represented by a FeedstockBatch. */
  FEEDSTOCK_BATCH,

  /** Material in the production lifecycle represented by a ProductionBatch. */
  PRODUCTION_BATCH
}
