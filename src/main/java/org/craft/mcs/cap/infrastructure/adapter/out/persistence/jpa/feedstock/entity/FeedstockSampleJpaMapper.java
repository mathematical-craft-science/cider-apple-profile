package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity;

import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.domain.chemical.value.PhValue;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.toCharacteristic;


public final class FeedstockSampleJpaMapper {

  private FeedstockSampleJpaMapper() {
  }

  public static @NonNull FeedstockSampleJpaEntity fromDomain(@NonNull FeedstockSample sample) {
    requireNonNull(sample, "feedstock sample cannot be null");

    final var profile = sample.chemicalProfile();

    return new FeedstockSampleJpaEntity(
        sample.sampleId().value(),
        sample.batchId().value(),
        sample.name().value(),
        sample.type(),
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

  public static @NonNull FeedstockSample toDomain(@NonNull FeedstockSampleJpaEntity entity) {
    requireNonNull(entity, "feedstock sample entity cannot be null");

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

    return FeedstockSample.create(
        FeedstockSampleId.of(requireNonNull(entity.sampleId, "feedstock sample id cannot be null")),
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        FeedstockSampleName.of(requireNonNull(entity.name, "feedstock sample name cannot be null")),
        requireNonNull(entity.type, "feedstock type cannot be null"),
        builder.build(),
        requireNonNull(entity.measuredAt, "measured at cannot be null")
    ).changeNote(entity.note);
  }
}
