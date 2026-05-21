package org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
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
    @NonNull CompositionContributionRole primaryRole,
    @NonNull List<@NonNull CompositionContributionEvidence> evidence,
    @NonNull Set<@NonNull ChemicalAxis> missingAxes
) {

  public CompositionContributionRoleAssessment {
    roles = List.copyOf(requireNonNull(roles, "composition contribution roles cannot be null"));
    evidence = List.copyOf(requireNonNull(evidence, "composition contribution evidence cannot be null"));
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "composition contribution missing axes cannot be null"));
    requireNonNull(primaryRole, "primary composition contribution role cannot be null");
  }

  public boolean assessable() {
    return !roles.contains(CompositionContributionRole.UNASSESSABLE);
  }
}
