package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Shared REST mapper for compact current-profile summary values.
 *
 * <p>It centralizes the transport flattening rule used by feedstock board and detail read models:
 * exact values stay exact, ranges expose midpoint plus bounds, and statistical values expose median
 * when present or mean otherwise.
 */
public final class FeedstockProfileSummaryRestMapper {

  private FeedstockProfileSummaryRestMapper() {
  }

  public static <T> @NonNull ProfileSummary<T> toProfileSummary(
      @NonNull ChemicalProfile profile,
      @NonNull ProfileValueFactory<T> factory
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    requireNonNull(factory, "profile value factory cannot be null");

    return new ProfileSummary<>(
        profile.sugar()
            .map(measurement -> toProfileValue(
                measurement,
                FeedstockProfileSummaryRestMapper::specificGravity,
                CanonicalUnitLabels.SUGAR,
                factory
            ))
            .orElse(null), // NOSONAR
        profile.titratableAcidity()
            .map(measurement -> toProfileValue(
                measurement,
                TitratableAcidityValue::asBigDecimal,
                CanonicalUnitLabels.TITRATABLE_ACIDITY,
                factory
            ))
            .orElse(null), // NOSONAR
        profile.ph()
            .map(measurement -> toProfileValue(
                measurement,
                PhValue::asBigDecimal,
                CanonicalUnitLabels.PH,
                factory
            ))
            .orElse(null), // NOSONAR
        profile.tanninity()
            .map(measurement -> toProfileValue(
                measurement,
                TanninityValue::asBigDecimal,
                CanonicalUnitLabels.TANNINITY,
                factory
            ))
            .orElse(null), // NOSONAR
        profile.yeastAssimilableNitrogen()
            .map(measurement -> toProfileValue(
                measurement,
                YeastAssimilableNitrogenValue::asBigDecimal,
                CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN,
                factory
            ))
            .orElse(null) // NOSONAR
    );
  }

  private static <T extends Comparable<T>, R> @NonNull R toProfileValue(
      @NonNull MeasuredCharacteristic<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    requireNonNull(measurement, "measured characteristic cannot be null");
    requireNonNull(extractor, "profile value extractor cannot be null");
    requireNonNull(unit, "profile value unit cannot be null");
    requireNonNull(factory, "profile value factory cannot be null");

    return switch (measurement) {
      case ExactMeasurement<T> exact -> exactProfileValue(exact, extractor, unit, factory);
      case RangeMeasurement<T> range -> rangeProfileValue(range, extractor, unit, factory);
      case StatisticalMeasurement<T> statistical -> statisticalProfileValue(statistical, extractor, unit, factory);
    };
  }

  private static <T extends Comparable<T>, R> @NonNull R exactProfileValue(
      @NonNull ExactMeasurement<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    return factory.create(
        quantity(extractor.apply(measurement.value()), unit),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }

  private static <T extends Comparable<T>, R> @NonNull R rangeProfileValue(
      @NonNull RangeMeasurement<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    final var min = extractor.apply(measurement.min());
    final var max = extractor.apply(measurement.max());
    return factory.create(
        quantity(rangeMidpoint(min, max), unit),
        MeasurementValueKind.RANGE_MIDPOINT,
        quantity(min, unit),
        quantity(max, unit),
        null,
        null
    );
  }

  private static <T extends Comparable<T>, R> @NonNull R statisticalProfileValue(
      @NonNull StatisticalMeasurement<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    final var hasMedian = measurement.median() != null;
    return factory.create(
        quantity(extractor.apply(statisticalValue(measurement, hasMedian)), unit),
        hasMedian ? MeasurementValueKind.STATISTICAL_MEDIAN : MeasurementValueKind.STATISTICAL_MEAN,
        null,
        null,
        nullableQuantity(measurement.standardDeviation(), unit),
        measurement.sampleSize()
    );
  }

  private static <T extends Comparable<T>> @NonNull T statisticalValue(
      @NonNull StatisticalMeasurement<T> measurement,
      boolean hasMedian
  ) {
    return hasMedian
        ? requireNonNull(measurement.median(), "statistical profile value median cannot be null")
        : measurement.mean();
  }

  private static @NonNull BigDecimal specificGravity(@NonNull SugarConcentration concentration) {
    requireNonNull(concentration, "sugar concentration cannot be null");
    return concentration.specificGravity().asBigDecimal();
  }

  private static @NonNull BigDecimal rangeMidpoint(
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    return min.add(max).divide(BigDecimal.valueOf(2), RoundingMode.UNNECESSARY);
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }

  private static @Nullable DecimalQuantityResponse nullableQuantity(
      @Nullable BigDecimal value,
      @NonNull String unit
  ) {
    return value == null ? null : quantity(value, unit);
  }

  public record ProfileSummary<T>(
      @Nullable T sugar,
      @Nullable T titratableAcidity,
      @Nullable T ph,
      @Nullable T tannin,
      @Nullable T yan
  ) {
  }

  @FunctionalInterface
  public interface ProfileValueFactory<T> {

    @NonNull
    T create(
        @NonNull DecimalQuantityResponse value,
        @NonNull MeasurementValueKind valueKind,
        @Nullable DecimalQuantityResponse min,
        @Nullable DecimalQuantityResponse max,
        @Nullable DecimalQuantityResponse standardDeviation,
        @Nullable Integer sampleSize
    );
  }
}
