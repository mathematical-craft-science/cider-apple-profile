package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity.AppleVarietyJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaAppleVarietyRepository implements AppleVarietyRepository {

  private final SpringDataAppleVarietyJpaRepository repository;

  @Override
  @NonNull
  public AppleVariety save(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var entity = AppleVarietyJpaMapper.fromDomain(appleVariety);
    final var saved = repository.save(entity);

    return AppleVarietyJpaMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull AppleVariety> findById(@NonNull AppleVarietyId varietyId) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    return repository.findById(varietyId.value())
        .map(AppleVarietyJpaMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull AppleVariety> findAll() {
    return repository.findAll()
        .stream()
        .map(AppleVarietyJpaMapper::toDomain)
        .toList();
  }
}
