package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.jspecify.annotations.NonNull;


/**
 * Delegates subject-stage measurement rules to the bounded context that owns the measured material.
 */
public interface MeasurementSubjectMeasurementPolicy {

  void validate(
      @NonNull MeasurementSubjectReference subject,
      @NonNull List<@NonNull MeasurementCharacteristicInput> measurements
  );
}
