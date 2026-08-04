package org.craft.mcs.cap.infrastructure.adapter.shared.measurement;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.jspecify.annotations.Nullable;


/** Maps optional transport or persistence strings to strongly typed measurement provenance values. */
public final class MeasurementProvenanceValueMapper {

  private MeasurementProvenanceValueMapper() {
  }

  public static @Nullable MeasurementSourceName sourceName(@Nullable String value) {
    return value == null ? null : MeasurementSourceName.of(value);
  }

  public static @Nullable MeasurementMethod method(@Nullable String value) {
    return value == null ? null : MeasurementMethod.of(value);
  }

  public static @Nullable MeasurementSourceReference reference(@Nullable String value) {
    return value == null ? null : MeasurementSourceReference.of(value);
  }

  public static @Nullable String sourceNameValue(@Nullable MeasurementSourceName sourceName) {
    return sourceName == null ? null : sourceName.value();
  }

  public static @Nullable String methodValue(@Nullable MeasurementMethod method) {
    return method == null ? null : method.value();
  }

  public static @Nullable String referenceValue(@Nullable MeasurementSourceReference reference) {
    return reference == null ? null : reference.value();
  }
}
