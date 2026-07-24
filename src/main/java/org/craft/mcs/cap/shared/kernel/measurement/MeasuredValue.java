package org.craft.mcs.cap.shared.kernel.measurement;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


/**
 * Common contract for immutable canonical measurement values.
 *
 * <p>Concrete values retain JSR-385 type safety through {@link AbstractMeasuredValue}; heterogeneous
 * measurement contexts depend only on this contract.</p>
 */
public interface MeasuredValue extends Comparable<MeasuredValue> {

  /** Returns the numeric value in the concrete value object's canonical domain unit. */
  @NonNull BigDecimal asBigDecimal();

  /**
   * Recreates the same concrete value-object type with a new canonical numeric value.
   *
   * <p>The implementation must preserve its characteristic and canonical unit and reapply all
   * value-object invariants.</p>
   */
  @NonNull MeasuredValue withValue(@NonNull BigDecimal value);
}
