package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.SugarConcentration;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.domain.measurement.value.PhValue;
import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.toCharacteristic;

public final class AppleVarietyJpaMapper {

  private AppleVarietyJpaMapper() {
  }

  public static @NonNull AppleVarietyJpaEntity fromDomain(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var profile = appleVariety.chemicalProfile();

    return new AppleVarietyJpaEntity(
        appleVariety.varietyId().value(),
        appleVariety.name().value(),
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

  public static @NonNull AppleVariety toDomain(@NonNull AppleVarietyJpaEntity entity) {
    requireNonNull(entity, "apple variety entity cannot be null");

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

    return AppleVariety.create(
        AppleVarietyId.of(entity.varietyId),
        AppleVarietyName.of(entity.name),
        builder.build()
    );
  }
}
