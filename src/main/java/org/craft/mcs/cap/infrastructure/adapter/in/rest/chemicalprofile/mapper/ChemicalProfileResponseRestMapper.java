package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;


/** Serializes canonical chemical-profile values without interpreting or converting their meaning. */
public final class ChemicalProfileResponseRestMapper {

  private ChemicalProfileResponseRestMapper() {
  }

  public static @NonNull ChemicalProfileResponse toResponse(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return new ChemicalProfileResponse(
        specificGravityResponse(profile),
        phResponse(profile),
        titratableAcidityResponse(profile),
        tanninResponse(profile),
        yeastAssimilableNitrogenResponse(profile)
    );
  }

  private static @Nullable ChemicalMeasurementResponse specificGravityResponse(
      @NonNull ChemicalProfile profile
  ) {
    return profile.sugar()
        .map(value -> toResponse(
            value,
            sugar -> sugar.specificGravity().asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse phResponse(@NonNull ChemicalProfile profile) {
    return profile.ph()
        .map(value -> toResponse(value, PhValue::asBigDecimal, CanonicalUnitLabels.PH))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse titratableAcidityResponse(
      @NonNull ChemicalProfile profile
  ) {
    return profile.titratableAcidity()
        .map(value -> toResponse(
            value,
            TitratableAcidityValue::asBigDecimal,
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse tanninResponse(@NonNull ChemicalProfile profile) {
    return profile.tannin()
        .map(value -> toResponse(value, TanninValue::asBigDecimal, CanonicalUnitLabels.TANNIN))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse yeastAssimilableNitrogenResponse(
      @NonNull ChemicalProfile profile
  ) {
    return profile.yeastAssimilableNitrogen()
        .map(value -> toResponse(
            value,
            YeastAssimilableNitrogenValue::asBigDecimal,
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        ))
        .orElse(null); // NOSONAR
  }

  private static <T extends Comparable<? super T>> @NonNull ChemicalMeasurementResponse toResponse(
      @NonNull CharacteristicValue<T> measurement,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    requireNonNull(measurement, "measurement cannot be null");
    requireNonNull(extractor, "canonical value extractor cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");

    return switch (measurement) {
      case PointCharacteristicValue<T>(var measuredValue) -> exactResponse(measuredValue, extractor, unit);
      case IntervalCharacteristicValue<T>(var minimum, var maximum) ->
          rangeResponse(minimum, maximum, extractor, unit);
    };
  }

  private static <T extends Comparable<? super T>> @NonNull ExactChemicalMeasurementResponse exactResponse(
      @NonNull T measuredValue,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    return new ExactChemicalMeasurementResponse(
        "EXACT",
        quantity(extractor.extract(measuredValue), unit)
    );
  }

  private static <T extends Comparable<? super T>> @NonNull RangeChemicalMeasurementResponse rangeResponse(
      @NonNull T minimum,
      @NonNull T maximum,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    return new RangeChemicalMeasurementResponse(
        "RANGE",
        quantity(extractor.extract(minimum), unit),
        quantity(extractor.extract(maximum), unit)
    );
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }

  @FunctionalInterface
  private interface CanonicalValueExtractor<T extends Comparable<? super T>> {

    @NonNull
    BigDecimal extract(@NonNull T value);
  }
}
