package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.catalog.application.AppleVarietyNameConflictException;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.catalog.entity.AppleVarietyJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcColumns;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementJdbcMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;


@Repository
@RequiredArgsConstructor
class JdbcAppleVarietyRepository implements AppleVarietyRepository {

  private final SpringDataAppleVarietyJdbcRepository repository;

  @Override
  @NonNull
  public AppleVariety create(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var saved = translateNameConflict(
        appleVariety,
        () -> translateOptimisticLocking(
            "apple variety",
            () -> repository.save(AppleVarietyJdbcMapper.fromDomain(appleVariety))
        )
    );

    return AppleVarietyJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<AppleVariety> update(
      @NonNull VersionedAggregate<AppleVariety> appleVariety
  ) {
    requireNonNull(appleVariety, "versioned apple variety cannot be null");
    final var aggregate = appleVariety.aggregate();
    final var profile = aggregate.referenceProfile();
    final var sugar = fromNullableCharacteristic(profile.specificGravity(), SpecificGravityValue::asBigDecimal);
    final var ph = fromNullableCharacteristic(profile.ph(), PhValue::asBigDecimal);
    final var acidity = fromNullableCharacteristic(
        profile.titratableAcidity(),
        TitratableAcidityValue::asBigDecimal
    );
    final var tannin = fromNullableCharacteristic(profile.tannin(), TanninValue::asBigDecimal);
    final var affectedRows = translateNameConflict(
        aggregate,
        () -> translateOptimisticLocking(
            "apple variety",
            () -> repository.updateByIdAndVersion(
                aggregate.varietyId().value(),
                aggregate.name().value(),
                kind(sugar),
                exactValue(sugar),
                minValue(sugar),
                maxValue(sugar),
                kind(ph),
                exactValue(ph),
                minValue(ph),
                maxValue(ph),
                kind(acidity),
                exactValue(acidity),
                minValue(acidity),
                maxValue(acidity),
                kind(tannin),
                exactValue(tannin),
                minValue(tannin),
                maxValue(tannin),
                aggregate.referenceProfileSource() == null
                    ? null
                    : aggregate.referenceProfileSource().value(),
                aggregate.referenceProfileConfidence(),
                appleVariety.revision().value()
            )
        )
    );
    return complete("apple variety", appleVariety, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<AppleVariety>> findVersionedById(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    return repository.findById(varietyId.value())
        .map(AppleVarietyJdbcMapper::toVersionedAggregate);
  }

  private static @Nullable MeasurementKind kind(@Nullable MeasurementJdbcColumns columns) {
    return columns == null ? null : columns.kind;
  }

  private static @Nullable BigDecimal exactValue(@Nullable MeasurementJdbcColumns columns) {
    return columns == null ? null : columns.exactValue;
  }

  private static @Nullable BigDecimal minValue(@Nullable MeasurementJdbcColumns columns) {
    return columns == null ? null : columns.minValue;
  }

  private static @Nullable BigDecimal maxValue(@Nullable MeasurementJdbcColumns columns) {
    return columns == null ? null : columns.maxValue;
  }

  private static <T extends @NonNull Object> T translateNameConflict(
      @NonNull AppleVariety appleVariety,
      @NonNull Supplier<T> operation
  ) {
    try {
      return operation.get();
    } catch (DuplicateKeyException exception) {
      final var message = exception.getMostSpecificCause().getMessage();
      if (message != null && message.contains("ux_apple_varieties_lower_name")) { // NOSONAR
        throw new AppleVarietyNameConflictException(appleVariety.name(), exception);
      }
      throw exception;
    }
  }
}
