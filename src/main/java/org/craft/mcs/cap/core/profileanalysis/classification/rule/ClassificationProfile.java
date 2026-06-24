package org.craft.mcs.cap.core.profileanalysis.classification.rule;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ClassificationProfile<C extends Enum<C>>(
    @NonNull String code,
    @NonNull List<@NonNull ClassificationRule<C>> rules
) {

  public ClassificationProfile {
    requireNonNull(code, "classification profile code cannot be null");
    requireNonNull(rules, "classification rules cannot be null");

    code = code.strip();
    if (code.isBlank()) {
      throw new DomainRuleViolationException("classification profile code cannot be blank");
    }

    rules = List.copyOf(rules);
    if (rules.isEmpty()) {
      throw new DomainRuleViolationException("classification profile must contain at least one rule");
    }
  }
}
