package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.SugarConcentration;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.AppleVarietySeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.ExactMeasurementSeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.MeasurementSeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.RangeMeasurementSeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.SeedMeasurementCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.StatisticalMeasurementSeedDto;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.domain.measurement.value.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.value.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.domain.measurement.value.PhValue;
import org.craft.mcs.cap.shared.domain.measurement.value.RangeMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.StatisticalMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

@Component
public final class AppleVarietyJsonSeedImporter {

  private static final TypeReference<List<AppleVarietySeedDto>> SEED_TYPE = new TypeReference<>() {
  };

  private final @NonNull ObjectMapper objectMapper;
  private final @NonNull AppleVarietyRepository repository;

  public AppleVarietyJsonSeedImporter(
      @NonNull ObjectMapper objectMapper,
      @NonNull AppleVarietyRepository repository
  ) {
    this.objectMapper = requireNonNull(objectMapper, "object mapper cannot be null");
    this.repository = requireNonNull(repository, "apple variety repository cannot be null");
  }

  public void importFrom(@NonNull InputStream inputStream) {
    requireNonNull(inputStream, "input stream cannot be null");

    final List<AppleVarietySeedDto> varieties;
    try {
      varieties = objectMapper.readValue(inputStream, SEED_TYPE);
    } catch (Exception exception) {
      throw new IllegalArgumentException("cannot read apple variety seed JSON", exception);
    }

    varieties.stream()
        .map(this::toDomain)
        .forEach(repository::save);
  }

  private @NonNull AppleVariety toDomain(@NonNull AppleVarietySeedDto dto) {
    requireNonNull(dto, "apple variety seed dto cannot be null");

    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of(dto.name()),
        toChemicalProfile(dto)
    );
  }

  private @NonNull ChemicalProfile toChemicalProfile(@NonNull AppleVarietySeedDto dto) {
    requireNonNull(dto, "apple variety seed dto cannot be null");

    final var builder = ChemicalProfile.builder();

    final var sugar = toMeasurement(
        dto.sugar(),
        SeedMeasurementCharacteristic.SUGAR,
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value))
    );
    if (sugar != null) {
      builder.sugar(sugar);
    }

    final var ph = toMeasurement(
        dto.ph(),
        SeedMeasurementCharacteristic.PH,
        PhValue::of
    );
    if (ph != null) {
      builder.ph(ph);
    }

    final var titratableAcidity = toMeasurement(
        dto.titratableAcidity(),
        SeedMeasurementCharacteristic.TITRATABLE_ACIDITY,
        TitratableAcidityValue::of
    );
    if (titratableAcidity != null) {
      builder.titratableAcidity(titratableAcidity);
    }

    final var tanninity = toMeasurement(
        dto.tanninity(),
        SeedMeasurementCharacteristic.TANNINITY,
        TanninityValue::of
    );
    if (tanninity != null) {
      builder.tanninity(tanninity);
    }

    if (dto.source() != null) {
      builder.source(ChemicalProfileSource.of(dto.source()));
    }

    builder.confidence(dto.confidence());

    return builder.build();
  }

  private static <T extends Comparable<T>> @Nullable MeasuredCharacteristic<T> toMeasurement(
      @Nullable MeasurementSeedDto dto,
      @NonNull SeedMeasurementCharacteristic characteristic,
      @NonNull StringValueFactory<T> factory
  ) {
    requireNonNull(characteristic, "seed measurement characteristic cannot be null");
    requireNonNull(factory, "value factory cannot be null");

    if (dto == null) {
      return null;
    }

    characteristic.validateUnit(dto.unit());

    return switch (dto) {
      case ExactMeasurementSeedDto exact -> ExactMeasurement.of(factory.create(exact.value()));

      case RangeMeasurementSeedDto range -> RangeMeasurement.of(
          factory.create(range.min()),
          factory.create(range.max())
      );

      case StatisticalMeasurementSeedDto statistical -> StatisticalMeasurement.of(
          factory.create(statistical.mean()),
          statistical.median() == null ? null : factory.create(statistical.median()),
          statistical.standardDeviation() == null ? null : new BigDecimal(statistical.standardDeviation()),
          statistical.sampleSize()
      );
    };
  }

  @FunctionalInterface
  private interface StringValueFactory<T extends Comparable<T>> {

    @NonNull
    T create(@NonNull String value);
  }
}