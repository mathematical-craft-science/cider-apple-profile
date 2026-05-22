package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity.AppleVarietyJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


@Repository
@RequiredArgsConstructor
class JdbcAppleVarietyRepository implements AppleVarietyRepository {

  private final SpringDataAppleVarietyJdbcRepository repository;

  @Override
  @NonNull
  public AppleVariety save(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var entity = repository.findById(appleVariety.varietyId().value())
        .map(existing -> AppleVarietyJdbcMapper.updateFromDomain(appleVariety, existing))
        .orElseGet(() -> AppleVarietyJdbcMapper.fromDomain(appleVariety));
    final var saved = translateOptimisticLocking("apple variety", () -> repository.save(entity));

    return AppleVarietyJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull AppleVariety> findById(@NonNull AppleVarietyId varietyId) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    return repository.findById(varietyId.value())
        .map(AppleVarietyJdbcMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull AppleVariety> findAll() {
    return repository.findAll()
        .stream()
        .map(AppleVarietyJdbcMapper::toDomain)
        .toList();
  }
}
