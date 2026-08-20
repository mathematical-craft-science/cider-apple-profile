package org.craft.mcs.cap.core.formulation.calculation.domain;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Describes one composition calculation from typed source characteristics.
 *
 * <p>The request selects policies for tannin completeness and interval representation while keeping
 * canonical measurement values inside each source.
 */
public record CompositionCalculationRequest(
    @NonNull List<@NonNull CompositionSource> sources,
    @NonNull CompositionTanninPolicy tanninPolicy,
    @NonNull CompositionIntervalPolicy intervalPolicy
) {

  public CompositionCalculationRequest {
    sources = List.copyOf(requireNonNull(sources, "composition sources cannot be null"));
    requireNonNull(tanninPolicy, "composition tannin policy cannot be null");
    requireNonNull(intervalPolicy, "composition interval policy cannot be null");

    CompositionWeightPolicy.requireValid(sources);
  }

  public CompositionCalculationRequest(
      @NonNull List<@NonNull CompositionSource> sources,
      @NonNull CompositionTanninPolicy tanninPolicy
  ) {
    this(sources, tanninPolicy, CompositionIntervalPolicy.MIDPOINT);
  }

  public static @NonNull CompositionCalculationRequest calculateTanninWhenComplete(
      @NonNull List<@NonNull CompositionSource> sources
  ) {
    return new CompositionCalculationRequest(
        sources,
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        CompositionIntervalPolicy.MIDPOINT
    );
  }
}
