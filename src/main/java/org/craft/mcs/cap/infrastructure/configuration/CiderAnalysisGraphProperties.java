package org.craft.mcs.cap.infrastructure.configuration;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;


/** External strategy selection for the sugar-acidity interpretation graph. */
@ConfigurationProperties(prefix = "cap.cider-analysis.sugar-acidity")
public record CiderAnalysisGraphProperties(
    /* Geometry used to classify graph points against built-in sugar-acidity zones. */
    @NonNull GraphZoneGeometryType geometry
) {

  public CiderAnalysisGraphProperties(
      @DefaultValue("RECTANGLE") @NonNull GraphZoneGeometryType geometry
  ) {
    this.geometry = requireNonNull(geometry, "sugar-acidity graph geometry type cannot be null");
  }

  /** Supported built-in sugar-acidity zone geometries. */
  public enum GraphZoneGeometryType {
    /** Classifies a point by axis-aligned rectangular zone boundaries. */
    RECTANGLE,

    /** Classifies a point by elliptical zone boundaries. */
    ELLIPSE
  }
}
