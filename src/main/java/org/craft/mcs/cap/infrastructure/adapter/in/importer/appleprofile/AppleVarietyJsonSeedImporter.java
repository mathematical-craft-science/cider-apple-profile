package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile;

import java.io.InputStream;
import java.util.List;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.in.CreateAppleVarietyCommand;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.in.CreateAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.AppleVarietySeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.ExactMeasurementSeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.MeasurementSeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.RangeMeasurementSeedDto;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto.SeedMeasurementCharacteristic;
import org.craft.mcs.cap.core.appleprofile.domain.model.ReferenceProfileSource;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

/**
 * Translates the local apple-variety JSON seed format into catalog creation commands.
 *
 * <p>The importer is an inbound adapter: JSON details remain outside the application, while writes
 * pass through {@link CreateAppleVarietyUseCase} instead of bypassing the use-case boundary.
 */
@Component
public final class AppleVarietyJsonSeedImporter {

  private static final TypeReference<List<AppleVarietySeedDto>> SEED_TYPE = new TypeReference<>() {
  };

  private final @NonNull ObjectMapper objectMapper;
  private final @NonNull CreateAppleVarietyUseCase createAppleVarietyUseCase;

  public AppleVarietyJsonSeedImporter(
      @NonNull ObjectMapper objectMapper,
      @NonNull CreateAppleVarietyUseCase createAppleVarietyUseCase
  ) {
    this.objectMapper = requireNonNull(objectMapper, "object mapper cannot be null");
    this.createAppleVarietyUseCase = requireNonNull(
        createAppleVarietyUseCase,
        "create apple variety use case cannot be null"
    );
  }

  public void importFrom(@NonNull InputStream inputStream) {
    requireNonNull(inputStream, "input stream cannot be null");

    final List<AppleVarietySeedDto> varieties;
    try {
      varieties = objectMapper.readValue(inputStream, SEED_TYPE);
    } catch (Exception exception) {
      throw new AppleVarietySeedImportException("Cannot read apple variety seed JSON", exception);
    }

    varieties.stream()
        .map(this::toCommand)
        .forEach(createAppleVarietyUseCase::create);
  }

  private @NonNull CreateAppleVarietyCommand toCommand(@NonNull AppleVarietySeedDto dto) {
    requireNonNull(dto, "apple variety seed dto cannot be null");

    return new CreateAppleVarietyCommand(
        AppleVarietyName.of(dto.name()),
        toReferenceProfile(dto),
        dto.source() == null ? null : ReferenceProfileSource.of(dto.source()), // NOSONAR
        dto.confidence()
    );
  }

  private @NonNull AppleVarietyReferenceProfile toReferenceProfile(@NonNull AppleVarietySeedDto dto) {
    requireNonNull(dto, "apple variety seed dto cannot be null");

    final var sugar = toMeasurement(
        dto.sugar(),
        SeedMeasurementCharacteristic.SUGAR,
        SpecificGravityValue::of
    );
    final var ph = toMeasurement(
        dto.ph(),
        SeedMeasurementCharacteristic.PH,
        PhValue::of
    );
    final var titratableAcidity = toMeasurement(
        dto.titratableAcidity(),
        SeedMeasurementCharacteristic.TITRATABLE_ACIDITY,
        TitratableAcidityValue::of
    );
    final var tannin = toMeasurement(
        dto.tannin(),
        SeedMeasurementCharacteristic.TANNIN,
        TanninValue::of
    );
    return new AppleVarietyReferenceProfile(sugar, titratableAcidity, ph, tannin);
  }

  private static <T extends Comparable<? super T>> @Nullable CharacteristicValue<T> toMeasurement(
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
      case ExactMeasurementSeedDto(_, _, var measuredValue) ->
          PointCharacteristicValue.of(factory.create(measuredValue));

      case RangeMeasurementSeedDto(_, _, var minimum, var maximum) -> IntervalCharacteristicValue.of(
          factory.create(minimum),
          factory.create(maximum)
      );

    };
  }

  @FunctionalInterface
  private interface StringValueFactory<T extends Comparable<? super T>> {

    @NonNull
    T create(@NonNull String value);
  }
}
