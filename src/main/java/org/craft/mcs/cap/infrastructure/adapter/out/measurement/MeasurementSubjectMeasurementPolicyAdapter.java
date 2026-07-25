package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ValidateFeedstockSpecificGravityUseCase;
import org.craft.mcs.cap.core.measurement.application.model.DirectMeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.RepeatedMeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectMeasurementPolicy;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * ACL adapter routing canonical measurement values to subject-owner policies.
 */
@Component
@RequiredArgsConstructor
final class MeasurementSubjectMeasurementPolicyAdapter
    implements MeasurementSubjectMeasurementPolicy {

  private final @NonNull ValidateFeedstockSpecificGravityUseCase feedstockSpecificGravityUseCase;

  @Override
  public void validate(
      @NonNull MeasurementSubjectReference subject,
      @NonNull List<@NonNull MeasurementCharacteristicInput> measurements
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    requireNonNull(measurements, "measurement inputs cannot be null");
    if (subject.type() == MeasurementSubjectType.FEEDSTOCK_BATCH) {
      measurements.stream()
          .filter(input -> input.characteristic() == MeasurementCharacteristic.SPECIFIC_GRAVITY)
          .forEach(this::validateFeedstockSpecificGravity);
    }
  }

  private void validateFeedstockSpecificGravity(@NonNull MeasurementCharacteristicInput input) {
    switch (input) {
      case DirectMeasurementCharacteristicInput(_, var value, _, _) -> validate(value);
      case RepeatedMeasurementCharacteristicInput(_, var observations, _) ->
          observations.forEach(observation -> validate(observation.value()));
    }
  }

  private void validate(
      @NonNull CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case PointCharacteristicValue<MeasuredValue>(var point) -> validate(point);
      case IntervalCharacteristicValue<MeasuredValue>(var lower, var upper) -> {
        validate(lower);
        validate(upper);
      }
    }
  }

  private void validate(@NonNull MeasuredValue value) {
    if (!(value instanceof SpecificGravityValue specificGravity)) {
      throw new ApplicationValidationException(
          "specific-gravity measurement requires a SpecificGravityValue"
      );
    }
    feedstockSpecificGravityUseCase.validate(specificGravity);
  }
}
