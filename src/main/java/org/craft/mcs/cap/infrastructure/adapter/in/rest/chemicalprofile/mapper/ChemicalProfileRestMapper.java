package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.ExternalChemicalMeasurementNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.PhQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.SugarQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.TanninityQuantityNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl.TitratableAcidityQuantityNormalizer;
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
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileRestMapper {

  private static final @NonNull String SG_UNIT = "SG";
  private static final @NonNull String PH_UNIT = "pH";
  private static final @NonNull String TITRATABLE_ACIDITY_UNIT = "g/L as malic acid";
  private static final @NonNull String TANNINITY_UNIT = "mg/L TAE";

  private static final @NonNull SugarQuantityNormalizer SUGAR_NORMALIZER = new SugarQuantityNormalizer();
  private static final @NonNull PhQuantityNormalizer PH_NORMALIZER = new PhQuantityNormalizer();
  private static final @NonNull TitratableAcidityQuantityNormalizer TITRATABLE_ACIDITY_NORMALIZER =
      new TitratableAcidityQuantityNormalizer();
  private static final @NonNull TanninityQuantityNormalizer TANNINITY_NORMALIZER = new TanninityQuantityNormalizer();

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

    if (request.source() != null) {
      builder.source(ChemicalProfileSource.of(request.source())); // NOSONAR
    }

    builder.confidence(request.confidence());

    return builder.build();
  }

  public static @NonNull ChemicalProfileResponse toResponse(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return new ChemicalProfileResponse(
        profile.sugar()
            .map(measurement -> toResponse(
                measurement,
                sugar -> sugar.specificGravity().asBigDecimal(),
                SG_UNIT
            ))
            .orElse(null),  // NOSONAR
        profile.ph()
            .map(measurement -> toResponse(measurement, PhValue::asBigDecimal, PH_UNIT))
            .orElse(null),  // NOSONAR
        profile.titratableAcidity()
            .map(measurement -> toResponse(
                measurement,
                TitratableAcidityValue::asBigDecimal,
                TITRATABLE_ACIDITY_UNIT
            ))
            .orElse(null),  // NOSONAR
        profile.tanninity()
            .map(measurement -> toResponse(measurement, TanninityValue::asBigDecimal, TANNINITY_UNIT))
            .orElse(null),  // NOSONAR
        profile.source() == null ? null : profile.source().value(),  // NOSONAR
        profile.confidence()
    );
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
          statistical.median() == null
          ? null
          : normalizer.normalizeValue(statistical.median(), statistical.unit()), // NOSONAR
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
      case ExactMeasurement<T> exact -> new ExactChemicalMeasurementResponse( // NOSONAR
          "EXACT",
          unit,
          extractor.extract(exact.value())
      );
      case RangeMeasurement<T> range -> new RangeChemicalMeasurementResponse( // NOSONAR
          "RANGE",
          unit,
          extractor.extract(range.min()),
          extractor.extract(range.max())
      );
      case StatisticalMeasurement<T> statistical -> new StatisticalChemicalMeasurementResponse( // NOSONAR
          "STATISTICAL",
          unit,
          extractor.extract(statistical.mean()),
          statistical.median() == null ? null : extractor.extract(statistical.median()), // NOSONAR
          statistical.standardDeviation(),
          statistical.sampleSize()
      );
    };
  }

  @FunctionalInterface
  private interface CanonicalValueExtractor<T extends Comparable<T>> {

    @NonNull
    BigDecimal extract(@NonNull T value);
  }
}
