package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JpaAppleVarietySugarAcidityPointCandidateRepositoryTest {

  @Test
  void normalizesNameContainsBeforePassingItToJpaQuery() {
    final var springDataRepository = mock(SpringDataAppleVarietyJpaRepository.class);
    final var repository = new JpaAppleVarietySugarAcidityPointCandidateRepository(springDataRepository);

    when(springDataRepository.findSugarAcidityPointCandidates(
        anyBoolean(),
        anyString(),
        anyBoolean(),
        anyString(),
        any(UUID.class),
        any(Pageable.class)
    ))
        .thenReturn(java.util.List.of());

    repository.findCandidates(new AppleVarietySugarAcidityPointCandidateQuery(
        new TextSearchCriteria("  Kingston  "),
        null,
        50
    ));

    verify(springDataRepository).findSugarAcidityPointCandidates(
        true,
        "kingston",
        false,
        "",
        new UUID(0L, 0L),
        Pageable.ofSize(50)
    );
  }

  @Test
  void passesNullNameContainsWhenSearchCriteriaIsAbsent() {
    final var springDataRepository = mock(SpringDataAppleVarietyJpaRepository.class);
    final var repository = new JpaAppleVarietySugarAcidityPointCandidateRepository(springDataRepository);

    when(springDataRepository.findSugarAcidityPointCandidates(
        anyBoolean(),
        anyString(),
        anyBoolean(),
        anyString(),
        any(UUID.class),
        any(Pageable.class)
    ))
        .thenReturn(java.util.List.of());

    repository.findCandidates(new AppleVarietySugarAcidityPointCandidateQuery(
        null,
        null,
        50
    ));

    verify(springDataRepository).findSugarAcidityPointCandidates(
        false,
        "",
        false,
        "",
        new UUID(0L, 0L),
        Pageable.ofSize(50)
    );
  }
}
