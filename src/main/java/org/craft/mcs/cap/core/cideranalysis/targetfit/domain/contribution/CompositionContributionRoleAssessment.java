package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Result of interpreting one profile source against a composition target.
 *
 * <p>The assessment is intentionally source-generic. It can describe a feedstock batch current
 * profile, apple variety profile, lab/manual source or any future profile-bearing source.
 */
public record CompositionContributionRoleAssessment(
    @NonNull List<@NonNull CompositionContributionRole> roles,
    @NonNull List<@NonNull CompositionContributionEvidence> evidence,
    @NonNull Set<@NonNull ProfileEvaluationAxis> missingAxes
) {

  public CompositionContributionRoleAssessment {
    roles = List.copyOf(requireNonNull(roles, "composition contribution roles cannot be null"));
    evidence = List.copyOf(requireNonNull(evidence, "composition contribution evidence cannot be null"));
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "composition contribution missing axes cannot be null"));
    if (roles.isEmpty()) {
      throw new DomainRuleViolationException("composition contribution assessment requires at least one role");
    }
    if (new HashSet<>(roles).size() != roles.size()) {
      throw new DomainRuleViolationException("composition contribution roles must be unique");
    }
    final var evidenceAxes = evidence.stream()
        .map(CompositionContributionEvidence::axis)
        .collect(Collectors.toSet());
    if (evidenceAxes.size() != evidence.size()) {
      throw new DomainRuleViolationException("composition contribution evidence axes must be unique");
    }
    if (missingAxes.stream().anyMatch(evidenceAxes::contains)) {
      throw new DomainRuleViolationException(
          "missing composition contribution axes cannot also have evidence"
      );
    }
    roles = Stream.of(CompositionContributionRole.values())
        .filter(roles::contains)
        .toList();
  }

  /**
   * Returns the highest-priority role without storing a second, potentially inconsistent value.
   */
  public @NonNull CompositionContributionRole primaryRole() {
    return roles.getFirst();
  }

}
