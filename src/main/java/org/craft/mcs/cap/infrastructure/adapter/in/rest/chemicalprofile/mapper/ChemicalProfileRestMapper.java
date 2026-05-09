package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalChemicalMeasurementRequest;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.domain.chemical.value.PhValue;
import org.craft.mcs.cap.shared.domain.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.domain.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.StatisticalMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileRestMapper {

  private ChemicalProfileRestMapper() {
  }

  public static @NonNull ChemicalProfile toDomain(@NonNull ChemicalProfileRequest request) {
    requireNonNull(request, "chemical profile request cannot be null");

    final var builder = ChemicalProfile.builder();

    final var sugar = toMeasurement(
        request.sugar(),
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value))
    );
    if (sugar != null) {
      builder.sugar(sugar);
    }

    final var ph = toMeasurement(request.ph(), PhValue::of);
    if (ph != null) {
      builder.ph(ph);
    }

    final var titratableAcidity = toMeasurement(
        request.titratableAcidity(),
        TitratableAcidityValue::of
    );
    if (titratableAcidity != null) {
      builder.titratableAcidity(titratableAcidity);
    }

    final var tanninity = toMeasurement(request.tanninity(), TanninityValue::of);
    if (tanninity != null) {
      builder.tanninity(tanninity);
    }

    if (request.source() != null) {
      builder.source(ChemicalProfileSource.of(request.source())); // NOSONAR
    }

    builder.confidence(request.confidence());

    return builder.build();
  }

  private static <T extends Comparable<T>> @Nullable MeasuredCharacteristic<T> toMeasurement(
      @Nullable ChemicalMeasurementRequest request,
      @NonNull BigDecimalValueFactory<T> factory
  ) {
    requireNonNull(factory, "measurement value factory cannot be null");

    if (request == null) {
      return null;
    }

    return switch (request) {
      case ExactChemicalMeasurementRequest exact -> ExactMeasurement.of(factory.create(exact.value()));
      case RangeChemicalMeasurementRequest range -> RangeMeasurement.of(
          factory.create(range.min()),
          factory.create(range.max())
      );
      case StatisticalChemicalMeasurementRequest statistical -> StatisticalMeasurement.of(
          factory.create(statistical.mean()),
          statistical.median() == null ? null : factory.create(statistical.median()), // NOSONAR
          statistical.standardDeviation(),
          statistical.sampleSize()
      );
    };
  }

  @FunctionalInterface
  private interface BigDecimalValueFactory<T extends Comparable<T>> {

    @NonNull
    T create(@NonNull BigDecimal value);
  }
}
