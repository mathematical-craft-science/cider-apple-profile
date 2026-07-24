package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Shared REST mapper for compact current-profile summary values.
 *
 * <p>It centralizes the transport flattening rule used by feedstock board and detail read models:
 * point values stay points and intervals expose a computed midpoint plus their original bounds.
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
        profile.tannin()
            .map(measurement -> toProfileValue(
                measurement,
                TanninValue::asBigDecimal,
                CanonicalUnitLabels.TANNIN,
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

  private static <T extends Comparable<? super T>, R> @NonNull R toProfileValue(
      @NonNull CharacteristicValue<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    requireNonNull(measurement, "measured characteristic cannot be null");
    requireNonNull(extractor, "profile value extractor cannot be null");
    requireNonNull(unit, "profile value unit cannot be null");
    requireNonNull(factory, "profile value factory cannot be null");

    return switch (measurement) {
      case PointCharacteristicValue<T>(var measuredValue) ->
          pointProfileValue(measuredValue, extractor, unit, factory);
      case IntervalCharacteristicValue<T>(var minimum, var maximum) ->
          intervalProfileValue(minimum, maximum, extractor, unit, factory);
    };
  }

  private static <T extends Comparable<? super T>, R> @NonNull R pointProfileValue(
      @NonNull T measuredValue,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    return factory.create(
        quantity(extractor.apply(measuredValue), unit),
        AxisValueKind.POINT,
        null,
        null
    );
  }

  private static <T extends Comparable<? super T>, R> @NonNull R intervalProfileValue(
      @NonNull T minimum,
      @NonNull T maximum,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit,
      @NonNull ProfileValueFactory<R> factory
  ) {
    final var min = extractor.apply(minimum);
    final var max = extractor.apply(maximum);
    return factory.create(
        quantity(intervalMidpoint(min, max), unit),
        AxisValueKind.INTERVAL_MIDPOINT,
        quantity(min, unit),
        quantity(max, unit)
    );
  }

  private static @NonNull BigDecimal specificGravity(@NonNull SugarConcentration concentration) {
    requireNonNull(concentration, "sugar concentration cannot be null");
    return concentration.specificGravity().asBigDecimal();
  }

  private static @NonNull BigDecimal intervalMidpoint(
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
        @NonNull AxisValueKind valueKind,
        @Nullable DecimalQuantityResponse min,
        @Nullable DecimalQuantityResponse max
    );
  }
}
