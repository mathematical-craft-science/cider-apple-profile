package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile;

import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietySugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JpaAppleVarietySugarAcidityPointCandidateRepositoryTest {

  @Test
  void normalizesNameContainsBeforePassingItToJpaQuery() {
    final var springDataRepository = mock(SpringDataAppleVarietyJpaRepository.class);
    final var repository = new JpaAppleVarietySugarAcidityPointCandidateRepository(springDataRepository);

    when(springDataRepository.findSugarAcidityPointCandidates(any(), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(java.util.List.of());

    repository.findCandidates(new AppleVarietySugarAcidityPointCandidateQuery(
        new TextSearchCriteria("  Kingston  "),
        null,
        50
    ));

    verify(springDataRepository).findSugarAcidityPointCandidates(
        "kingston",
        null,
        null,
        Pageable.ofSize(50)
    );
  }

  @Test
  void passesNullNameContainsWhenSearchCriteriaIsAbsent() {
    final var springDataRepository = mock(SpringDataAppleVarietyJpaRepository.class);
    final var repository = new JpaAppleVarietySugarAcidityPointCandidateRepository(springDataRepository);

    when(springDataRepository.findSugarAcidityPointCandidates(isNull(), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(java.util.List.of());

    repository.findCandidates(new AppleVarietySugarAcidityPointCandidateQuery(
        null,
        null,
        50
    ));

    verify(springDataRepository).findSugarAcidityPointCandidates(
        null,
        null,
        null,
        Pageable.ofSize(50)
    );
  }
}
