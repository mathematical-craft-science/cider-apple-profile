package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.ExternalMeasurementConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.PhQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.SpecificGravityQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.TanninQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.TitratableAcidityQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.YeastAssimilableNitrogenQuantityConverter;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Converts external chemical-profile request quantities to CAP canonical value objects. */
public final class ChemicalProfileRequestRestMapper {

  private static final @NonNull SpecificGravityQuantityConverter SPECIFIC_GRAVITY_CONVERTER =
      new SpecificGravityQuantityConverter();
  private static final @NonNull PhQuantityConverter PH_CONVERTER = new PhQuantityConverter();
  private static final @NonNull TitratableAcidityQuantityConverter TITRATABLE_ACIDITY_CONVERTER =
      new TitratableAcidityQuantityConverter();
  private static final @NonNull TanninQuantityConverter TANNIN_CONVERTER =
      new TanninQuantityConverter();
  private static final @NonNull YeastAssimilableNitrogenQuantityConverter
      YEAST_ASSIMILABLE_NITROGEN_CONVERTER = new YeastAssimilableNitrogenQuantityConverter();

  private ChemicalProfileRequestRestMapper() {
  }

  public static @NonNull ChemicalProfile toDomain(@NonNull ChemicalProfileRequest request) {
    requireNonNull(request, "chemical profile request cannot be null");

    final var builder = ChemicalProfile.builder();

    final var specificGravity = toLegacySugarMeasurement(
        request.specificGravity(),
        SPECIFIC_GRAVITY_CONVERTER
    );
    if (specificGravity != null) {
      builder.sugar(specificGravity);
    }

    final var ph = toMeasurement(request.ph(), PH_CONVERTER);
    if (ph != null) {
      builder.ph(ph);
    }

    final var titratableAcidity = toMeasurement(
        request.titratableAcidity(),
        TITRATABLE_ACIDITY_CONVERTER
    );
    if (titratableAcidity != null) {
      builder.titratableAcidity(titratableAcidity);
    }

    final var tannin = toMeasurement(request.tannin(), TANNIN_CONVERTER);
    if (tannin != null) {
      builder.tannin(tannin);
    }

    final var yeastAssimilableNitrogen = toMeasurement(
        request.yeastAssimilableNitrogen(),
        YEAST_ASSIMILABLE_NITROGEN_CONVERTER
    );
    if (yeastAssimilableNitrogen != null) {
      builder.yeastAssimilableNitrogen(yeastAssimilableNitrogen);
    }

    return builder.build();
  }

  private static <T extends Comparable<? super T>> @Nullable CharacteristicValue<T> toMeasurement(
      @Nullable ChemicalMeasurementRequest request,
      @NonNull ExternalMeasurementConverter<T> converter
  ) {
    requireNonNull(converter, "external measurement converter cannot be null");

    if (request == null) {
      return null;
    }

    return switch (request) {
      case ExactChemicalMeasurementRequest(_, var unit, var measuredValue) -> PointCharacteristicValue.of(
          converter.convertValue(measuredValue, unit)
      );
      case RangeChemicalMeasurementRequest(_, var unit, var minimum, var maximum) -> IntervalCharacteristicValue.of(
          converter.convertValue(minimum, unit),
          converter.convertValue(maximum, unit)
      );
    };
  }

  private static @Nullable CharacteristicValue<SugarConcentration> toLegacySugarMeasurement(
      @Nullable ChemicalMeasurementRequest request,
      @NonNull ExternalMeasurementConverter<SpecificGravityValue> converter
  ) {
    final var specificGravity = toMeasurement(request, converter);
    if (specificGravity == null) {
      return null;
    }

    return switch (specificGravity) {
      case PointCharacteristicValue<SpecificGravityValue>(var value) ->
          PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(value));
      case IntervalCharacteristicValue<SpecificGravityValue>(var lower, var upper) ->
          IntervalCharacteristicValue.of(
              SugarConcentration.ofSpecificGravity(lower),
              SugarConcentration.ofSpecificGravity(upper)
          );
    };
  }
}
