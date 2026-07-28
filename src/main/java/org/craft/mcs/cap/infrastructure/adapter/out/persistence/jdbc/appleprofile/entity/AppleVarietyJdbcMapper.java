package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.toCharacteristic;

public final class AppleVarietyJdbcMapper {

  private AppleVarietyJdbcMapper() {
  }

  public static @NonNull AppleVarietyJdbcEntity fromDomain(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var profile = appleVariety.chemicalProfile();

    return new AppleVarietyJdbcEntity(
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
            profile.tannin().orElse(null),
            TanninValue::asBigDecimal
        ),
        appleVariety.chemicalProfileSource() == null
            ? null
            : appleVariety.chemicalProfileSource().value(),
        appleVariety.chemicalProfileConfidence()
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

    entity.chemicalProfileSource = updated.chemicalProfileSource;
    entity.chemicalProfileConfidence = updated.chemicalProfileConfidence;

    return entity;
  }

  public static @NonNull AppleVariety toDomain(@NonNull AppleVarietyJdbcEntity entity) {
    requireNonNull(entity, "apple variety row cannot be null");

    return AppleVariety.create(
        AppleVarietyId.of(entity.varietyId),
        AppleVarietyName.of(entity.name),
        toChemicalProfile(entity),
        entity.chemicalProfileSource == null
            ? null
            : ChemicalProfileSource.of(entity.chemicalProfileSource),
        entity.chemicalProfileConfidence
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

  private static @NonNull ChemicalProfile toChemicalProfile(@NonNull AppleVarietyJdbcEntity entity) {
    requireNonNull(entity, "apple variety row cannot be null");

    final var builder = ChemicalProfile.builder();

    toCharacteristic(
        entity.sugar(),
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value.toPlainString()))
    ).ifPresent(builder::sugar);

    toCharacteristic(
        entity.ph(),
        value -> PhValue.of(value.toPlainString())
    ).ifPresent(builder::ph);

    toCharacteristic(
        entity.titratableAcidity(),
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).ifPresent(builder::titratableAcidity);

    toCharacteristic(
        entity.tannin(),
        value -> TanninValue.of(value.toPlainString())
    ).ifPresent(builder::tannin);

    return builder.build();
  }

}
