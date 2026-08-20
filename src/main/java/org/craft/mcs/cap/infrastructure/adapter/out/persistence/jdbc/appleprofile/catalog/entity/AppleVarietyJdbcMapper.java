package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog.entity;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.ReferenceProfileSource;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueJdbcMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.CharacteristicValueJdbcMapperSupport.toCharacteristic;

public final class AppleVarietyJdbcMapper {

  private AppleVarietyJdbcMapper() {
  }

  public static @NonNull AppleVarietyJdbcEntity fromDomain(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var profile = appleVariety.referenceProfile();
    final var referenceProfileSource = appleVariety.referenceProfileSource();

    return new AppleVarietyJdbcEntity(
        appleVariety.varietyId().value(),
        appleVariety.name().value(),
        fromNullableCharacteristic(
            profile.specificGravity(),
            SpecificGravityValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.ph(),
            PhValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.titratableAcidity(),
            TitratableAcidityValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.tannin(),
            TanninValue::asBigDecimal
        ),
        referenceProfileSource == null
            ? null
            : referenceProfileSource.value(),
        appleVariety.referenceProfileConfidence()
    );
  }

  public static @NonNull AppleVariety toDomain(@NonNull AppleVarietyJdbcEntity entity) {
    requireNonNull(entity, "apple variety row cannot be null");

    return AppleVariety.create(
        AppleVarietyId.of(entity.varietyId),
        AppleVarietyName.of(entity.name),
        toReferenceProfile(entity),
        entity.referenceProfileSource == null
            ? null
            : ReferenceProfileSource.of(entity.referenceProfileSource),
        entity.referenceProfileConfidence
    );
  }

  public static @NonNull VersionedAggregate<AppleVariety> toVersionedAggregate(
      @NonNull AppleVarietyJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  private static @NonNull AppleVarietyReferenceProfile toReferenceProfile(
      @NonNull AppleVarietyJdbcEntity entity
  ) {
    requireNonNull(entity, "apple variety row cannot be null");

    return new AppleVarietyReferenceProfile(
        toCharacteristic(
            entity.sugar(),
            value -> SpecificGravityValue.of(value.toPlainString())
        ).orElse(null), // NOSONAR
        toCharacteristic(
            entity.titratableAcidity(),
            value -> TitratableAcidityValue.of(value.toPlainString())
        ).orElse(null), // NOSONAR
        toCharacteristic(
            entity.ph(),
            value -> PhValue.of(value.toPlainString())
        ).orElse(null), // NOSONAR
        toCharacteristic(
            entity.tannin(),
            value -> TanninValue.of(value.toPlainString())
        ).orElse(null) // NOSONAR
    );
  }

}
