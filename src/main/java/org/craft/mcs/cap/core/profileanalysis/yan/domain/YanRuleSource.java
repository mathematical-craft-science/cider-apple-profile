package org.craft.mcs.cap.core.profileanalysis.yan.domain;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Curated source marker for YAN business rules.
 *
 * <p>The source is metadata for explainability. It documents which cider/enology references
 * informed the default thresholds, while CAP owns the business interpretation.
 */
public record YanRuleSource(
    @NonNull String code,
    @NonNull String comment
) {

  public static final @NonNull String CURATED_CIDER_V1 = "CURATED_CIDER_YAN_RULES_V1";

  public static final @NonNull String CURATED_CIDER_V1_COMMENT =
      "Curated from cider nitrogen references, Bisson/Butzke stuck-fermentation guidance, "
          + "AWRI/Lallemand YAN guidance, and UC Davis Brix-based nutrient recommendations.";

  public YanRuleSource {
    requireNonNull(code, "YAN rule source code cannot be null");
    requireNonNull(comment, "YAN rule source comment cannot be null");
  }

  public static @NonNull YanRuleSource curatedCiderV1() {
    return new YanRuleSource(CURATED_CIDER_V1, CURATED_CIDER_V1_COMMENT);
  }
}
