package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Curated source marker for pH business rules.
 *
 * <p>The source is intentionally metadata for explainability. It documents which cider references
 * informed the default thresholds, while the policy itself remains a business rule owned by CAP.
 */
public record PhRuleSource(
    @NonNull String code,
    @NonNull String comment
) {

  public static final @NonNull String CURATED_CIDER_V1 = "CURATED_CIDER_PH_RULES_V1";

  public static final @NonNull String CURATED_CIDER_V1_COMMENT =
      "Curated from Merwin/Valois/Padilla-Zakour, Jolicoeur, WSU/MSU cider materials, and future "
          + "yeast manufacturer datasheets for strain-aware rules.";

  public PhRuleSource {
    requireNonNull(code, "pH rule source code cannot be null");
    requireNonNull(comment, "pH rule source comment cannot be null");
  }

  public static @NonNull PhRuleSource curatedCiderV1() {
    return new PhRuleSource(CURATED_CIDER_V1, CURATED_CIDER_V1_COMMENT);
  }
}
