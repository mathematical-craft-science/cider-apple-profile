package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.ExternalMeasurementConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.PhQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.SugarQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.TanninQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.TitratableAcidityQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.YeastAssimilableNitrogenQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileRestMapper {

  private static final @NonNull SugarQuantityConverter SUGAR_CONVERTER = new SugarQuantityConverter();
  private static final @NonNull PhQuantityConverter PH_CONVERTER = new PhQuantityConverter();
  private static final @NonNull TitratableAcidityQuantityConverter TITRATABLE_ACIDITY_CONVERTER =
      new TitratableAcidityQuantityConverter();
  private static final @NonNull TanninQuantityConverter TANNIN_CONVERTER =
      new TanninQuantityConverter();
  private static final @NonNull YeastAssimilableNitrogenQuantityConverter
      YEAST_ASSIMILABLE_NITROGEN_CONVERTER = new YeastAssimilableNitrogenQuantityConverter();

  private ChemicalProfileRestMapper() {
  }

  public static @NonNull ChemicalProfile toDomain(@NonNull ChemicalProfileRequest request) {
    requireNonNull(request, "chemical profile request cannot be null");

    final var builder = ChemicalProfile.builder();

    final var sugar = toMeasurement(
        request.sugar(),
        SUGAR_CONVERTER
    );
    if (sugar != null) {
      builder.sugar(sugar);
    }

    final var ph = toMeasurement(
        request.ph(),
        PH_CONVERTER
    );
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

    final var tannin = toMeasurement(
        request.tannin(),
        TANNIN_CONVERTER
    );
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

  public static @NonNull ChemicalProfileResponse toResponse(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return new ChemicalProfileResponse(
        sugarResponse(profile),
        phResponse(profile),
        titratableAcidityResponse(profile),
        tanninResponse(profile),
        yeastAssimilableNitrogenResponse(profile)
    );
  }

  private static @Nullable ChemicalMeasurementResponse sugarResponse(@NonNull ChemicalProfile profile) {
    final var measurement = profile.sugar();
    return measurement.map(
        profileValue -> toResponse(profileValue,
            sugar -> sugar.specificGravity().asBigDecimal(), CanonicalUnitLabels.SUGAR))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse phResponse(@NonNull ChemicalProfile profile) {
    final var measurement = profile.ph();
    return measurement.map(
        profileValue -> toResponse(profileValue, PhValue::asBigDecimal,
            CanonicalUnitLabels.PH))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse titratableAcidityResponse(
      @NonNull ChemicalProfile profile
  ) {
    final var measurement = profile.titratableAcidity();
    return measurement.map(
        profileValue -> toResponse(profileValue,
            TitratableAcidityValue::asBigDecimal, CanonicalUnitLabels.TITRATABLE_ACIDITY))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse tanninResponse(@NonNull ChemicalProfile profile) {
    final var measurement = profile.tannin();
    return measurement.map(profileValue -> toResponse(profileValue,
        TanninValue::asBigDecimal, CanonicalUnitLabels.TANNIN))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse yeastAssimilableNitrogenResponse(
      @NonNull ChemicalProfile profile
  ) {
    final var measurement = profile.yeastAssimilableNitrogen();
    return measurement.map(profileValue -> toResponse(
        profileValue,
        YeastAssimilableNitrogenValue::asBigDecimal,
        CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
    )).orElse(null); // NOSONAR
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
