package org.craft.mcs.cap.infrastructure.configuration;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;


/** External strategy selection for sugar-acidity profile analysis. */
@ConfigurationProperties(prefix = "cap.profile-analysis.sugar-acidity")
public record ProfileAnalysisSugarAcidityProperties(
    /* Geometry used to classify chemical-profile points against built-in sugar-acidity zones. */
    @NonNull SugarAcidityAssessorType assessor
) {

  public ProfileAnalysisSugarAcidityProperties(
      @DefaultValue("RECTANGLE") @NonNull SugarAcidityAssessorType assessor
  ) {
    this.assessor = requireNonNull(assessor, "sugar-acidity assessor type cannot be null");
  }

  /** Supported built-in sugar-acidity zone geometries. */
  public enum SugarAcidityAssessorType {
    /** Classifies a point by axis-aligned rectangular zone boundaries. */
    RECTANGLE,

    /** Classifies a point by elliptical zone boundaries. */
    ELLIPSE
  }
}
