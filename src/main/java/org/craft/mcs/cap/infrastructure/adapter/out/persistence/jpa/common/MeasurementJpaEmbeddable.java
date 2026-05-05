package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


@Embeddable
public class MeasurementJpaEmbeddable {

  @Nullable
  @Enumerated(EnumType.STRING)
  @Column(name = "measurement_kind")
  MeasurementKind kind;

  @Nullable
  @Column(name = "exact_value", precision = 12, scale = 4)
  BigDecimal exactValue;

  @Nullable
  @Column(name = "min_value", precision = 12, scale = 4)
  BigDecimal minValue;

  @Nullable
  @Column(name = "max_value", precision = 12, scale = 4)
  BigDecimal maxValue;

  @Nullable
  @Column(name = "mean_value", precision = 12, scale = 4)
  BigDecimal meanValue;

  @Nullable
  @Column(name = "median_value", precision = 12, scale = 4)
  BigDecimal medianValue;

  @Nullable
  @Column(name = "standard_deviation", precision = 12, scale = 4)
  BigDecimal standardDeviation;

  @Nullable
  @Column(name = "sample_size")
  Integer sampleSize;

  public MeasurementJpaEmbeddable() {
  }
}
