package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity;

import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.toCharacteristic;


public final class FeedstockMeasurementJpaMapper {

  private FeedstockMeasurementJpaMapper() {
  }

  public static @NonNull FeedstockMeasurementJpaEntity fromDomain(@NonNull FeedstockMeasurement sample) {
    requireNonNull(sample, "feedstock measurement cannot be null");

    final var profile = sample.chemicalProfile();

    return new FeedstockMeasurementJpaEntity(
        sample.measurementId().value(),
        sample.batchId().value(),
        sample.name().value(),
        sample.measuredAt(),
        sample.note(),
        fromNullableCharacteristic(
            profile.sugar().orElse(null),
            sugar -> sugar.specificGravity().asBigDecimal()
        ),
        fromNullableCharacteristic(
            profile.ph().orElse(null),
            PhValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.titratableAcidity().orElse(null),
            TitratableAcidityValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.tanninity().orElse(null),
            TanninityValue::asBigDecimal
        ),
        profile.source() == null ? null : profile.source().value(),
        profile.confidence()
    );
  }

  public static @NonNull FeedstockMeasurement toDomain(@NonNull FeedstockMeasurementJpaEntity entity) {
    requireNonNull(entity, "feedstock measurement entity cannot be null");

    final var builder = ChemicalProfile.builder();

    toCharacteristic(
        entity.sugar,
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value.toPlainString()))
    ).ifPresent(builder::sugar);

    toCharacteristic(
        entity.ph,
        value -> PhValue.of(value.toPlainString())
    ).ifPresent(builder::ph);

    toCharacteristic(
        entity.titratableAcidity,
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).ifPresent(builder::titratableAcidity);

    toCharacteristic(
        entity.tanninity,
        value -> TanninityValue.of(value.toPlainString())
    ).ifPresent(builder::tanninity);

    if (entity.chemicalProfileSource != null) {
      builder.source(ChemicalProfileSource.of(entity.chemicalProfileSource));
    }

    builder.confidence(entity.chemicalProfileConfidence);

    return FeedstockMeasurement.create(
        FeedstockMeasurementId.of(requireNonNull(entity.measurementId, "feedstock measurement id cannot be null")),
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        FeedstockMeasurementName.of(requireNonNull(entity.name, "feedstock measurement name cannot be null")),
        builder.build(),
        requireNonNull(entity.measuredAt, "measured at cannot be null")
    ).changeNote(entity.note);
  }
}
