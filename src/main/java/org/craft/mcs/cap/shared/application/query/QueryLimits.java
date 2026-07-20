package org.craft.mcs.cap.shared.application.query;


/** Common safeguards for bounded application queries; feature-specific lower bounds remain local. */
public final class QueryLimits {

  public static final int DEFAULT = 50;
  public static final int MAXIMUM = 200;
  public static final int PAGE_LOOKAHEAD = 1;
  public static final int MAXIMUM_PAGE_FETCH = MAXIMUM + PAGE_LOOKAHEAD;

  private QueryLimits() {
  }
}
