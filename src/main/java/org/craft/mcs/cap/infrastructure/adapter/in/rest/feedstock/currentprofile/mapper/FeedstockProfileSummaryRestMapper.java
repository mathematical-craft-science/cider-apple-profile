package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.mapper;

import java.math.BigDecimal;
import java.util.function.Function;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileSummaryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Maps typed Feedstock current characteristics to their compact REST summary.
 *
 * <p>Point values stay points and intervals expose a computed midpoint plus their original bounds.
 */
final class FeedstockProfileSummaryRestMapper {

  private FeedstockProfileSummaryRestMapper() {
  }

  static @NonNull FeedstockCurrentProfileSummaryResponse toResponse(
      @NonNull FeedstockCurrentProfileSummaryView view
  ) {
    requireNonNull(view, "feedstock current profile summary cannot be null");
    return new FeedstockCurrentProfileSummaryResponse(
        view.specificGravity() == null
            ? null
            : toProfileValue( // NOSONAR
                view.specificGravity(),
                SpecificGravityValue::asBigDecimal,
                CanonicalUnitLabels.SPECIFIC_GRAVITY
            ),
        view.titratableAcidity() == null
            ? null
            : toProfileValue( // NOSONAR
                view.titratableAcidity(),
                TitratableAcidityValue::asBigDecimal,
                CanonicalUnitLabels.TITRATABLE_ACIDITY
            ),
        view.ph() == null
            ? null
            : toProfileValue( // NOSONAR
                view.ph(),
                PhValue::asBigDecimal,
                CanonicalUnitLabels.PH
            ),
        view.tannin() == null
            ? null
            : toProfileValue( // NOSONAR
                view.tannin(),
                TanninValue::asBigDecimal,
                CanonicalUnitLabels.TANNIN
            ),
        view.yeastAssimilableNitrogen() == null
            ? null
            : toProfileValue( // NOSONAR
                view.yeastAssimilableNitrogen(),
                YeastAssimilableNitrogenValue::asBigDecimal,
                CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
            )
    );
  }

  private static <T extends Comparable<? super T>> @NonNull FeedstockCurrentProfileValueResponse toProfileValue(
      @NonNull CharacteristicValue<T> measurement,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    requireNonNull(measurement, "measured characteristic cannot be null");
    requireNonNull(extractor, "profile value extractor cannot be null");
    requireNonNull(unit, "profile value unit cannot be null");

    return switch (measurement) {
      case PointCharacteristicValue<T>(var measuredValue) ->
          pointProfileValue(measuredValue, extractor, unit);
      case IntervalCharacteristicValue<T>(var minimum, var maximum) ->
          intervalProfileValue(minimum, maximum, extractor, unit);
    };
  }

  private static <T extends Comparable<? super T>> @NonNull FeedstockCurrentProfileValueResponse pointProfileValue(
      @NonNull T measuredValue,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    return new FeedstockCurrentProfileValueResponse(
        quantity(extractor.apply(measuredValue), unit),
        ProfileCharacteristicValueKind.POINT,
        null,
        null
    );
  }

  private static <T extends Comparable<? super T>> @NonNull FeedstockCurrentProfileValueResponse intervalProfileValue(
      @NonNull T minimum,
      @NonNull T maximum,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor,
      @NonNull String unit
  ) {
    final var min = extractor.apply(minimum);
    final var max = extractor.apply(maximum);
    return new FeedstockCurrentProfileValueResponse(
        quantity(midpoint(min, max), unit),
        ProfileCharacteristicValueKind.INTERVAL_MIDPOINT,
        quantity(min, unit),
        quantity(max, unit)
    );
  }

  private static @NonNull DecimalQuantityResponse quantity(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return new DecimalQuantityResponse(value, unit);
  }
}
