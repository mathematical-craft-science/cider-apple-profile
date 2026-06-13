package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.ExternalChemicalMeasurementNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.PhQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.SugarQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.TanninityQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.TitratableAcidityQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.YeastAssimilableNitrogenQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalChemicalMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileRestMapper {

  private static final @NonNull SugarQuantityNormalizer SUGAR_NORMALIZER = new SugarQuantityNormalizer();
  private static final @NonNull PhQuantityNormalizer PH_NORMALIZER = new PhQuantityNormalizer();
  private static final @NonNull TitratableAcidityQuantityNormalizer TITRATABLE_ACIDITY_NORMALIZER =
      new TitratableAcidityQuantityNormalizer();
  private static final @NonNull TanninityQuantityNormalizer TANNINITY_NORMALIZER = new TanninityQuantityNormalizer();
  private static final @NonNull YeastAssimilableNitrogenQuantityNormalizer
      YEAST_ASSIMILABLE_NITROGEN_NORMALIZER = new YeastAssimilableNitrogenQuantityNormalizer();

  private ChemicalProfileRestMapper() {
  }

  public static @NonNull ChemicalProfile toDomain(@NonNull ChemicalProfileRequest request) {
    requireNonNull(request, "chemical profile request cannot be null");

    final var builder = ChemicalProfile.builder();

    final var sugar = toMeasurement(
        request.sugar(),
        SUGAR_NORMALIZER
    );
    if (sugar != null) {
      builder.sugar(sugar);
    }

    final var ph = toMeasurement(
        request.ph(),
        PH_NORMALIZER
    );
    if (ph != null) {
      builder.ph(ph);
    }

    final var titratableAcidity = toMeasurement(
        request.titratableAcidity(),
        TITRATABLE_ACIDITY_NORMALIZER
    );
    if (titratableAcidity != null) {
      builder.titratableAcidity(titratableAcidity);
    }

    final var tanninity = toMeasurement(
        request.tanninity(),
        TANNINITY_NORMALIZER
    );
    if (tanninity != null) {
      builder.tanninity(tanninity);
    }

    final var yeastAssimilableNitrogen = toMeasurement(
        request.yeastAssimilableNitrogen(),
        YEAST_ASSIMILABLE_NITROGEN_NORMALIZER
    );
    if (yeastAssimilableNitrogen != null) {
      builder.yeastAssimilableNitrogen(yeastAssimilableNitrogen);
    }

    applySource(builder, request.source());

    builder.confidence(request.confidence());

    return builder.build();
  }

  public static @NonNull ChemicalProfileResponse toResponse(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return new ChemicalProfileResponse(
        sugarResponse(profile),
        phResponse(profile),
        titratableAcidityResponse(profile),
        tanninityResponse(profile),
        yeastAssimilableNitrogenResponse(profile),
        sourceResponse(profile),
        profile.confidence()
    );
  }

  private static void applySource(
      ChemicalProfile.@NonNull Builder builder,
      @Nullable String source
  ) {
    if (source != null) {
      builder.source(ChemicalProfileSource.of(source));
    }
  }

  private static @Nullable ChemicalMeasurementResponse sugarResponse(@NonNull ChemicalProfile profile) {
    final var measurement = profile.sugar();
    return measurement.map(
        sugarConcentrationMeasuredCharacteristic -> toResponse(sugarConcentrationMeasuredCharacteristic,
            sugar -> sugar.specificGravity().asBigDecimal(), CanonicalUnitLabels.SUGAR))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse phResponse(@NonNull ChemicalProfile profile) {
    final var measurement = profile.ph();
    return measurement.map(
        phValueMeasuredCharacteristic -> toResponse(phValueMeasuredCharacteristic, PhValue::asBigDecimal,
            CanonicalUnitLabels.PH))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse titratableAcidityResponse(
      @NonNull ChemicalProfile profile
  ) {
    final var measurement = profile.titratableAcidity();
    return measurement.map(
        titratableAcidityValueMeasuredCharacteristic -> toResponse(titratableAcidityValueMeasuredCharacteristic,
            TitratableAcidityValue::asBigDecimal, CanonicalUnitLabels.TITRATABLE_ACIDITY))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse tanninityResponse(@NonNull ChemicalProfile profile) {
    final var measurement = profile.tanninity();
    return measurement.map(tanninityValueMeasuredCharacteristic -> toResponse(tanninityValueMeasuredCharacteristic,
        TanninityValue::asBigDecimal, CanonicalUnitLabels.TANNINITY))
        .orElse(null); // NOSONAR
  }

  private static @Nullable ChemicalMeasurementResponse yeastAssimilableNitrogenResponse(
      @NonNull ChemicalProfile profile
  ) {
    final var measurement = profile.yeastAssimilableNitrogen();
    return measurement.map(yeastAssimilableNitrogenValueMeasuredCharacteristic -> toResponse(
        yeastAssimilableNitrogenValueMeasuredCharacteristic,
        YeastAssimilableNitrogenValue::asBigDecimal,
        CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
    )).orElse(null); // NOSONAR
  }

  private static @Nullable String sourceResponse(@NonNull ChemicalProfile profile) {
    final var source = profile.source();
    return source == null ? null : source.value();
  }

  private static <T extends Comparable<T>> @Nullable MeasuredCharacteristic<T> toMeasurement(
      @Nullable ChemicalMeasurementRequest request,
      @NonNull ExternalChemicalMeasurementNormalizer<T> normalizer
  ) {
    requireNonNull(normalizer, "external measurement normalizer cannot be null");

    if (request == null) {
      return null;
    }

    return switch (request) {
      case ExactChemicalMeasurementRequest exact -> ExactMeasurement.of(
          normalizer.normalizeValue(exact.value(), exact.unit())
      );
      case RangeChemicalMeasurementRequest range -> RangeMeasurement.of(
          normalizer.normalizeValue(range.min(), range.unit()),
          normalizer.normalizeValue(range.max(), range.unit())
      );
      case StatisticalChemicalMeasurementRequest statistical -> StatisticalMeasurement.of(
          normalizer.normalizeValue(statistical.mean(), statistical.unit()),
          normalizedMedian(statistical, normalizer),
          normalizer.normalizeStandardDeviation(
              statistical.mean(),
              statistical.standardDeviation(),
              statistical.unit()
          ),
          statistical.sampleSize()
      );
    };
  }

  private static <T extends Comparable<T>> @NonNull ChemicalMeasurementResponse toResponse(
      @NonNull MeasuredCharacteristic<T> measurement,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    requireNonNull(measurement, "measurement cannot be null");
    requireNonNull(extractor, "canonical value extractor cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");

    return switch (measurement) {
      case ExactMeasurement<T> exact -> exactResponse(exact, extractor, unit);
      case RangeMeasurement<T> range -> rangeResponse(range, extractor, unit);
      case StatisticalMeasurement<T> statistical -> statisticalResponse(statistical, extractor, unit);
    };
  }

  private static <T extends Comparable<T>> @Nullable T normalizedMedian(
      @NonNull StatisticalChemicalMeasurementRequest statistical,
      @NonNull ExternalChemicalMeasurementNormalizer<T> normalizer
  ) {
    final var median = statistical.median();
    return median == null
        ? null
        : normalizer.normalizeValue(median, statistical.unit());
  }

  private static <T extends Comparable<T>> @NonNull ExactChemicalMeasurementResponse exactResponse(
      @NonNull ExactMeasurement<T> exact,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    return new ExactChemicalMeasurementResponse(
        "EXACT",
        quantity(extractor.extract(exact.value()), unit)
    );
  }

  private static <T extends Comparable<T>> @NonNull RangeChemicalMeasurementResponse rangeResponse(
      @NonNull RangeMeasurement<T> range,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    return new RangeChemicalMeasurementResponse(
        "RANGE",
        quantity(extractor.extract(range.min()), unit),
        quantity(extractor.extract(range.max()), unit)
    );
  }

  private static <T extends Comparable<T>> @NonNull StatisticalChemicalMeasurementResponse statisticalResponse(
      @NonNull StatisticalMeasurement<T> statistical,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    return new StatisticalChemicalMeasurementResponse(
        "STATISTICAL",
        quantity(extractor.extract(statistical.mean()), unit),
        medianQuantity(statistical, extractor, unit),
        optionalQuantity(statistical.standardDeviation(), unit),
        statistical.sampleSize()
    );
  }

  private static <T extends Comparable<T>> @Nullable DecimalQuantityResponse medianQuantity(
      @NonNull StatisticalMeasurement<T> statistical,
      @NonNull CanonicalValueExtractor<T> extractor,
      @NonNull String unit
  ) {
    final var median = statistical.median();
    return median == null ? null : quantity(extractor.extract(median), unit);
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }

  private static @Nullable DecimalQuantityResponse optionalQuantity(
      @Nullable BigDecimal value,
      @NonNull String unit
  ) {
    return value == null ? null : new DecimalQuantityResponse(value, unit);
  }

  @FunctionalInterface
  private interface CanonicalValueExtractor<T extends Comparable<T>> {

    @NonNull
    BigDecimal extract(@NonNull T value);
  }
}
