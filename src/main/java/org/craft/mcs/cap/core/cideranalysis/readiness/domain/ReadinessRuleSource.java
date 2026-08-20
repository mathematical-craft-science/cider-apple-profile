package org.craft.mcs.cap.core.cideranalysis.readiness.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Curated source metadata explaining the evidence behind a readiness policy. */
public record ReadinessRuleSource(
    @NonNull String code,
    @NonNull String comment
) {

  private static final @NonNull String CURATED_PH_V1 = "CURATED_CIDER_PH_RULES_V1";
  private static final @NonNull String CURATED_PH_V1_COMMENT =
      "Curated from Merwin/Valois/Padilla-Zakour, Jolicoeur, WSU/MSU cider materials, and future "
          + "yeast manufacturer datasheets for strain-aware rules.";

  private static final @NonNull String CURATED_YAN_V1 = "CURATED_CIDER_YAN_RULES_V1";
  private static final @NonNull String CURATED_YAN_V1_COMMENT =
      "Curated from cider nitrogen references, Bisson/Butzke stuck-fermentation guidance, "
          + "AWRI/Lallemand YAN guidance, and UC Davis Brix-based nutrient recommendations.";

  public ReadinessRuleSource {
    requireNonNull(code, "readiness rule source code cannot be null");
    requireNonNull(comment, "readiness rule source comment cannot be null");
    if (code.isBlank() || comment.isBlank()) {
      throw new DomainRuleViolationException("readiness rule source fields cannot be blank");
    }
  }

  public static @NonNull ReadinessRuleSource curatedPhV1() {
    return new ReadinessRuleSource(CURATED_PH_V1, CURATED_PH_V1_COMMENT);
  }

  public static @NonNull ReadinessRuleSource curatedYanV1() {
    return new ReadinessRuleSource(CURATED_YAN_V1, CURATED_YAN_V1_COMMENT);
  }
}
