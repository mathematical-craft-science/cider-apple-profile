package org.craft.mcs.cap.core.feedstock.application.detail.model;


/**
 * Last known operation reflected by the current profile read model.
 */
public enum FeedstockBatchCurrentProfileOperationType {

  /**
   * The latest known profile-changing event is a measurement. Material operations are not inferred
   * until the feedstock operations/traceability slice records them explicitly.
   */
  MEASURED
}
