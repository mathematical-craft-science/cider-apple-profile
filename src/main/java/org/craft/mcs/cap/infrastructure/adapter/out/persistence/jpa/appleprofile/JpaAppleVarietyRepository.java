package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity.AppleVarietyJpaEntity;
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

    final var entity = AppleVarietyJpaEntity.fromDomain(appleVariety);
    final var saved = repository.save(entity);

    return saved.toDomain();
  }
}
