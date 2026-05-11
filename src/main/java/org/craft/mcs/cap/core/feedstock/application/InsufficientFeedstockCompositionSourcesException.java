package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;


public final class InsufficientFeedstockCompositionSourcesException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final int sourceCount;
  private final int minimumSourceCount;

  public InsufficientFeedstockCompositionSourcesException(
      int sourceCount,
      int minimumSourceCount
  ) {
    if (sourceCount < 0) {
      throw new IllegalArgumentException("feedstock composition source count cannot be negative");
    }
    if (minimumSourceCount < 1) {
      throw new IllegalArgumentException("minimum feedstock composition source count must be positive");
    }
    super("feedstock composition optimization requires at least "
        + minimumSourceCount
        + " usable sources, but found "
        + sourceCount);
    this.sourceCount = sourceCount;
    this.minimumSourceCount = minimumSourceCount;
  }

  public int sourceCount() {
    return sourceCount;
  }

  public int minimumSourceCount() {
    return minimumSourceCount;
  }
}
