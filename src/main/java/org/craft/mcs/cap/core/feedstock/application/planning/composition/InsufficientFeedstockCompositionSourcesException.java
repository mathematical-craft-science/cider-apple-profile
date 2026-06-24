package org.craft.mcs.cap.core.feedstock.application.planning.composition;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


public final class InsufficientFeedstockCompositionSourcesException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final int sourceCount;
  private final int minimumSourceCount;

  public InsufficientFeedstockCompositionSourcesException(
      int sourceCount,
      int minimumSourceCount
  ) {
    super(message(sourceCount, minimumSourceCount));
    this.sourceCount = sourceCount;
    this.minimumSourceCount = minimumSourceCount;
  }

  public int sourceCount() {
    return sourceCount;
  }

  public int minimumSourceCount() {
    return minimumSourceCount;
  }

  @NonNull
  private static String message(
      int sourceCount,
      int minimumSourceCount
  ) {
    if (sourceCount < 0) {
      throw new ApplicationValidationException("feedstock composition source count cannot be negative");
    }
    if (minimumSourceCount < 1) {
      throw new ApplicationValidationException("minimum feedstock composition source count must be positive");
    }

    return "feedstock composition optimization requires at least "
        + minimumSourceCount
        + " usable sources, but found "
        + sourceCount;
  }
}
