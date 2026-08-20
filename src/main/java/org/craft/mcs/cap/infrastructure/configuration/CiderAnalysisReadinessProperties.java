package org.craft.mcs.cap.infrastructure.configuration;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;


/**
 * External defaults for CiderAnalysis readiness policies.
 *
 * <p>The configured values tune CAP's curated business rules. They are not the authoritative
 * source of pH or YAN science; domain policies still define how these values are interpreted.
 */
@ConfigurationProperties(prefix = "cap.cider-analysis.readiness")
public record CiderAnalysisReadinessProperties(
    /* Curated pH thresholds used by planning guidance and fermentation-start readiness. */
    @NonNull PhProperties ph,
    /* Curated YAN thresholds used by fermentation-start readiness. */
    @NonNull YanProperties yan
) {

  public CiderAnalysisReadinessProperties {
    requireNonNull(ph, "cider-analysis pH readiness properties cannot be null");
    requireNonNull(yan, "cider-analysis YAN readiness properties cannot be null");
  }

  /**
   * Default pH thresholds and source marker for staged planning/readiness assessment.
   */
  public record PhProperties(
      /* Generic lower pH tolerance used when no yeast-specific tolerance is available. */
      @NonNull BigDecimal genericYeastToleranceMin,
      /* Lower bound of the curated cider pH guidance range. */
      @NonNull BigDecimal ciderGuidanceMin,
      /* Upper bound of the curated cider pH guidance range. */
      @NonNull BigDecimal ciderGuidanceMax,
      /* pH at or above which CAP reports elevated microbial risk. */
      @NonNull BigDecimal highMicrobialRisk,
      /* Stable identifier of the curated pH rule set. */
      @NonNull String sourceCode,
      /* Human-readable provenance note for the configured pH rule set. */
      @NonNull String sourceComment
  ) {

    public PhProperties(
        @DefaultValue("2.8") @NonNull BigDecimal genericYeastToleranceMin,
        @DefaultValue("3.3") @NonNull BigDecimal ciderGuidanceMin,
        @DefaultValue("3.8") @NonNull BigDecimal ciderGuidanceMax,
        @DefaultValue("4.0") @NonNull BigDecimal highMicrobialRisk,
        @DefaultValue("CURATED_CIDER_PH_RULES_V1") @NonNull String sourceCode,
        @DefaultValue("Curated cider pH business rules v1") @NonNull String sourceComment
    ) {
      this.genericYeastToleranceMin =
          requireNonNull(genericYeastToleranceMin, "generic yeast pH tolerance minimum cannot be null");
      this.ciderGuidanceMin = requireNonNull(ciderGuidanceMin, "cider pH guidance minimum cannot be null");
      this.ciderGuidanceMax = requireNonNull(ciderGuidanceMax, "cider pH guidance maximum cannot be null");
      this.highMicrobialRisk = requireNonNull(highMicrobialRisk, "high pH microbial-risk threshold cannot be null");
      this.sourceCode = requireNonNull(sourceCode, "pH rule source code cannot be null");
      this.sourceComment = requireNonNull(sourceComment, "pH rule source comment cannot be null");
    }
  }

  /**
   * Default YAN thresholds and source marker for fermentation-start readiness assessment.
   */
  public record YanProperties(
      /* YAN below which CAP reports a severe nitrogen deficiency. */
      @NonNull BigDecimal severeLow,
      /* YAN below which CAP reports a nitrogen deficiency. */
      @NonNull BigDecimal low,
      /* YAN above which CAP reports an elevated nitrogen level. */
      @NonNull BigDecimal high,
      /* Stable identifier of the curated YAN rule set. */
      @NonNull String sourceCode,
      /* Human-readable provenance note for the configured YAN rule set. */
      @NonNull String sourceComment
  ) {

    public YanProperties(
        @DefaultValue("75") @NonNull BigDecimal severeLow,
        @DefaultValue("150") @NonNull BigDecimal low,
        @DefaultValue("400") @NonNull BigDecimal high,
        @DefaultValue("CURATED_CIDER_YAN_RULES_V1") @NonNull String sourceCode,
        @DefaultValue("Curated cider YAN business rules v1") @NonNull String sourceComment
    ) {
      this.severeLow = requireNonNull(severeLow, "severe low YAN threshold cannot be null");
      this.low = requireNonNull(low, "low YAN threshold cannot be null");
      this.high = requireNonNull(high, "high YAN threshold cannot be null");
      this.sourceCode = requireNonNull(sourceCode, "YAN rule source code cannot be null");
      this.sourceComment = requireNonNull(sourceComment, "YAN rule source comment cannot be null");
    }
  }
}
