package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietySugarAcidityPointCandidateRepository;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity.AppleVarietyJpaMapper;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaAppleVarietySugarAcidityPointCandidateRepository
    implements AppleVarietySugarAcidityPointCandidateRepository {

  private static final @NonNull String NO_CURSOR_NAME = "";
  private static final @NonNull UUID NO_CURSOR_ID = new UUID(0L, 0L);

  private final @NonNull SpringDataAppleVarietyJpaRepository repository;

  @Override
  public @NonNull List<@NonNull AppleVariety> findCandidates(
      @NonNull AppleVarietySugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "apple variety point candidate query cannot be null");

    final var cursor = query.after();
    final var nameContains = normalizeNameContains(query.nameContains());

    return repository.findSugarAcidityPointCandidates(
            nameContains != null,
            nameContains == null ? "" : nameContains,
            cursor != null,
            cursor == null ? NO_CURSOR_NAME : cursor.name(),
            cursor == null ? NO_CURSOR_ID : cursor.varietyId(),
            PageRequest.ofSize(query.limit())
        )
        .stream()
        .map(AppleVarietyJpaMapper::toDomain)
        .toList();
  }

  private static @Nullable String normalizeNameContains(@Nullable TextSearchCriteria criteria) {
    if (criteria == null) {
      return null;
    }

    final var normalized = criteria.query().strip().toLowerCase(Locale.ROOT);
    return normalized.isBlank() ? null : normalized;
  }
}
