package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Service
public class ChemicalProfileAxisExtractor {

  private static final int MIDPOINT_SCALE = 6;

  private static <T extends Comparable<? super T>> @Nullable AxisValue toAxisValue(
      @NonNull ChemicalAxis axis,
      @Nullable CharacteristicValue<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(extractor, "axis value extractor cannot be null");

    return switch (characteristic) {
      case null -> null;
      case PointCharacteristicValue<T>(T value) -> pointAxisValue(axis, value, extractor);
      case IntervalCharacteristicValue<T>(T min, T max) -> intervalAxisValue(axis, min, max, extractor);
    };
  }

  public @NonNull ChemicalProfileAxes extract(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return ChemicalProfileAxes.of(
        profile.sugar()
            .map(characteristic -> toAxisValue(
                ChemicalAxis.SUGAR,
                characteristic,
                value -> value.specificGravity().asBigDecimal()
            ))
            .orElse(null),
        profile.titratableAcidity()
            .map(characteristic -> toAxisValue(
                ChemicalAxis.ACIDITY,
                characteristic,
                TitratableAcidityValue::asBigDecimal
            ))
            .orElse(null),
        profile.tannin()
            .map(characteristic -> toAxisValue(ChemicalAxis.TANNIN, characteristic, TanninValue::asBigDecimal))
            .orElse(null),
        profile.yeastAssimilableNitrogen()
            .map(characteristic -> toAxisValue(
                ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
                characteristic,
                YeastAssimilableNitrogenValue::asBigDecimal
            ))
            .orElse(null)
    );
  }

  private static <T extends Comparable<? super T>> @NonNull AxisValue pointAxisValue(
      @NonNull ChemicalAxis axis,
      @NonNull T value,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    return new AxisValue(
        axis,
        extractor.apply(value),
        AxisValueKind.POINT,
        null,
        null
    );
  }

  private static <T extends Comparable<? super T>> @NonNull AxisValue intervalAxisValue(
      @NonNull ChemicalAxis axis,
      @NonNull T minValue,
      @NonNull T maxValue,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    final var min = extractor.apply(minValue);
    final var max = extractor.apply(maxValue);
    final var midpoint = min.add(max).divide(BigDecimal.valueOf(2), MIDPOINT_SCALE, RoundingMode.HALF_UP);

    return new AxisValue(
        axis,
        midpoint,
        AxisValueKind.INTERVAL_MIDPOINT,
        min,
        max
    );
  }

}
