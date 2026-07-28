package org.craft.mcs.cap.shared.kernel.sugar.converter;

import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;

/**
 * Converts between analytical scales used to estimate sugar in unfermented juice or must.
 *
 * <p>Implementations define the scientific assumptions of each conversion. The source and target
 * measurement values themselves remain neutral and do not imply sugar semantics.</p>
 */
public interface SugarConversionStrategy<F extends MeasuredValue, T extends MeasuredValue> {

  Class<F> sourceType();

  Class<T> targetType();

  T convert(F source);

  default boolean supports(
      Class<?> source,
      Class<?> target
  ) {
    return sourceType().equals(source) && targetType().equals(target);
  }
}
