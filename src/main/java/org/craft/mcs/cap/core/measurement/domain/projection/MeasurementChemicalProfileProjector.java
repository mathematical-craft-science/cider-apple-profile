package org.craft.mcs.cap.core.measurement.domain.projection;

import java.util.Map;
import java.util.Optional;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Projects canonical generic measurement results into the immutable chemical profile used by analysis. */
public final class MeasurementChemicalProfileProjector {

  private MeasurementChemicalProfileProjector() {
  }

  public static @NonNull Optional<@NonNull ChemicalProfile> project(
      @NonNull Map<
          @NonNull MeasurementCharacteristic,
          @NonNull CharacteristicValue<MeasuredValue>
      > values
  ) {
    requireNonNull(values, "measurement values cannot be null");
    if (values.keySet().stream().noneMatch(MeasurementChemicalProfileProjector::isChemical)) {
      return Optional.empty();
    }

    final var builder = ChemicalProfile.builder();
    applySugar(builder, values.get(MeasurementCharacteristic.SPECIFIC_GRAVITY));
    applyPh(builder, values.get(MeasurementCharacteristic.PH));
    applyAcidity(builder, values.get(MeasurementCharacteristic.TITRATABLE_ACIDITY));
    applyTannin(builder, values.get(MeasurementCharacteristic.TANNIN));
    applyYan(builder, values.get(MeasurementCharacteristic.YAN));
    return Optional.of(builder.build());
  }

  public static boolean isChemical(@NonNull MeasurementCharacteristic characteristic) {
    return switch (requireNonNull(characteristic, "measurement characteristic cannot be null")) {
      case SPECIFIC_GRAVITY, PH, TITRATABLE_ACIDITY, TANNIN, YAN -> true;
      case TEMPERATURE, TURBIDITY -> false;
    };
  }

  private static void applySugar(
      ChemicalProfile.@NonNull Builder builder,
      @Nullable CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case null -> {
        // do nothing
      }
      case PointCharacteristicValue<MeasuredValue>(SpecificGravityValue specificGravity) ->
          builder.sugar(PointCharacteristicValue.of(
              SugarConcentration.ofSpecificGravity(specificGravity)
          ));
      case IntervalCharacteristicValue<MeasuredValue>(
          SpecificGravityValue lower,
          SpecificGravityValue upper
      ) ->
          builder.sugar(IntervalCharacteristicValue.of(
              SugarConcentration.ofSpecificGravity(lower),
              SugarConcentration.ofSpecificGravity(upper)
          ));
      default -> throw incompatible(MeasurementCharacteristic.SPECIFIC_GRAVITY);
    }
  }

  private static void applyPh(
      ChemicalProfile.@NonNull Builder builder,
      @Nullable CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case null -> {
        // do nothing
      }
      case PointCharacteristicValue<MeasuredValue>(PhValue ph) ->
          builder.ph(PointCharacteristicValue.of(ph));
      case IntervalCharacteristicValue<MeasuredValue>(PhValue lower, PhValue upper) ->
          builder.ph(IntervalCharacteristicValue.of(lower, upper));
      default -> throw incompatible(MeasurementCharacteristic.PH);
    }
  }

  private static void applyAcidity(
      ChemicalProfile.@NonNull Builder builder,
      @Nullable CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case null -> {
        // do nothing
      }
      case PointCharacteristicValue<MeasuredValue>(TitratableAcidityValue acidity) ->
          builder.titratableAcidity(PointCharacteristicValue.of(acidity));
      case IntervalCharacteristicValue<MeasuredValue>(
          TitratableAcidityValue lower,
          TitratableAcidityValue upper
      ) ->
          builder.titratableAcidity(IntervalCharacteristicValue.of(
              lower,
              upper
          ));
      default -> throw incompatible(MeasurementCharacteristic.TITRATABLE_ACIDITY);
    }
  }

  private static void applyTannin(
      ChemicalProfile.@NonNull Builder builder,
      @Nullable CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case null -> {
        // do nothing
      }
      case PointCharacteristicValue<MeasuredValue>(TanninValue tannin) ->
          builder.tannin(PointCharacteristicValue.of(tannin));
      case IntervalCharacteristicValue<MeasuredValue>(TanninValue lower, TanninValue upper) ->
          builder.tannin(IntervalCharacteristicValue.of(lower, upper));
      default -> throw incompatible(MeasurementCharacteristic.TANNIN);
    }
  }

  private static void applyYan(
      ChemicalProfile.@NonNull Builder builder,
      @Nullable CharacteristicValue<MeasuredValue> value
  ) {
    switch (value) {
      case null -> {
        // do nothing
      }
      case PointCharacteristicValue<MeasuredValue>(YeastAssimilableNitrogenValue yan) ->
          builder.yeastAssimilableNitrogen(PointCharacteristicValue.of(yan));
      case IntervalCharacteristicValue<MeasuredValue>(
          YeastAssimilableNitrogenValue lower,
          YeastAssimilableNitrogenValue upper
      ) -> builder.yeastAssimilableNitrogen(IntervalCharacteristicValue.of(lower, upper));
      default -> throw incompatible(MeasurementCharacteristic.YAN);
    }
  }

  private static @NonNull DomainRuleViolationException incompatible(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return new DomainRuleViolationException("measurement value type does not match " + characteristic);
  }
}
