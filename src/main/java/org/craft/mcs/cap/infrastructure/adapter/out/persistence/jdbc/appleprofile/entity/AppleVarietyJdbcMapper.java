package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.appleprofile.domain.model.ReferenceProfileSource;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.toCharacteristic;

public final class AppleVarietyJdbcMapper {

  private AppleVarietyJdbcMapper() {
  }

  public static @NonNull AppleVarietyJdbcEntity fromDomain(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var profile = appleVariety.referenceProfile();

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
        appleVariety.referenceProfileSource() == null
            ? null
            : appleVariety.referenceProfileSource().value(),
        appleVariety.referenceProfileConfidence()
    );
  }

  public static @NonNull AppleVarietyJdbcEntity updateFromDomain(
      @NonNull AppleVariety appleVariety,
      @NonNull AppleVarietyJdbcEntity entity
  ) {
    requireNonNull(appleVariety, "apple variety cannot be null");
    requireNonNull(entity, "apple variety row cannot be null");

    final var updated = fromDomain(appleVariety);

    entity.name = updated.name;

    entity.sugarKind = updated.sugarKind;
    entity.sugarExactValue = updated.sugarExactValue;
    entity.sugarMinValue = updated.sugarMinValue;
    entity.sugarMaxValue = updated.sugarMaxValue;

    entity.phKind = updated.phKind;
    entity.phExactValue = updated.phExactValue;
    entity.phMinValue = updated.phMinValue;
    entity.phMaxValue = updated.phMaxValue;

    entity.titratableAcidityKind = updated.titratableAcidityKind;
    entity.titratableAcidityExactValue = updated.titratableAcidityExactValue;
    entity.titratableAcidityMinValue = updated.titratableAcidityMinValue;
    entity.titratableAcidityMaxValue = updated.titratableAcidityMaxValue;

    entity.tanninKind = updated.tanninKind;
    entity.tanninExactValue = updated.tanninExactValue;
    entity.tanninMinValue = updated.tanninMinValue;
    entity.tanninMaxValue = updated.tanninMaxValue;

    entity.referenceProfileSource = updated.referenceProfileSource;
    entity.referenceProfileConfidence = updated.referenceProfileConfidence;

    return entity;
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

  public static long revision(@NonNull AppleVarietyJdbcEntity entity) {
    return requireNonNull(entity, "apple variety row cannot be null").version;
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
