package org.craft.mcs.cap.core.cideranalysis.classification.domain.engine;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Typed characteristic input shared by cider-apple classification strategies.
 *
 * <p>Each strategy declares which optional characteristics are required for its own rules.</p>
 */
@ValueObject
public record CiderAppleClassificationInput(
    @Nullable CharacteristicValue<SpecificGravityValue> sugarContent,
    @Nullable CharacteristicValue<TitratableAcidityValue> acidity,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public CiderAppleClassificationInput {
    if (sugarContent == null && acidity == null && tannin == null) {
      throw new DomainRuleViolationException("cider apple classification input cannot be empty");
    }
  }

  public @NonNull Set<@NonNull ClassificationCharacteristic> missing(
      @NonNull Set<@NonNull ClassificationCharacteristic> required
  ) {
    final var missing = EnumSet.noneOf(ClassificationCharacteristic.class);
    missing.addAll(requireNonNull(required, "required classification characteristics cannot be null"));
    missing.removeIf(this::has);
    return Set.copyOf(missing);
  }

  public boolean has(@NonNull ClassificationCharacteristic characteristic) {
    return switch (requireNonNull(characteristic, "classification characteristic cannot be null")) {
      case SUGAR_CONTENT -> sugarContent != null;
      case ACIDITY -> acidity != null;
      case TANNIN -> tannin != null;
    };
  }

  public @NonNull BigDecimal representativeValue(
      @NonNull ClassificationCharacteristic characteristic,
      @NonNull ClassificationIntervalPolicy intervalPolicy
  ) {
    requireNonNull(intervalPolicy, "classification interval policy cannot be null");
    return switch (requireNonNull(characteristic, "classification characteristic cannot be null")) {
      case SUGAR_CONTENT -> intervalPolicy.representativeValue(requirePresent(sugarContent, characteristic));
      case ACIDITY -> intervalPolicy.representativeValue(requirePresent(acidity, characteristic));
      case TANNIN -> intervalPolicy.representativeValue(requirePresent(tannin, characteristic));
    };
  }

  public boolean isPoint(@NonNull ClassificationCharacteristic characteristic) {
    return switch (requireNonNull(characteristic, "classification characteristic cannot be null")) {
      case SUGAR_CONTENT -> sugarContent instanceof PointCharacteristicValue<SpecificGravityValue>;
      case ACIDITY -> acidity instanceof PointCharacteristicValue<TitratableAcidityValue>;
      case TANNIN -> tannin instanceof PointCharacteristicValue<TanninValue>;
    };
  }

  public @NonNull Optional<@NonNull ClassificationInterval> interval(
      @NonNull ClassificationCharacteristic characteristic
  ) {
    return switch (requireNonNull(characteristic, "classification characteristic cannot be null")) {
      case SUGAR_CONTENT -> interval(sugarContent, SpecificGravityValue::asBigDecimal);
      case ACIDITY -> interval(acidity, TitratableAcidityValue::asBigDecimal);
      case TANNIN -> interval(tannin, TanninValue::asBigDecimal);
    };
  }

  private static <T extends Comparable<? super T>> @NonNull CharacteristicValue<T> requirePresent(
      @Nullable CharacteristicValue<T> value,
      @NonNull ClassificationCharacteristic characteristic
  ) {
    if (value == null) {
      throw new DomainRuleViolationException("missing classification characteristic: " + characteristic);
    }
    return value;
  }

  private static <T extends Comparable<? super T>> @NonNull Optional<@NonNull ClassificationInterval> interval(
      @Nullable CharacteristicValue<T> value,
      @NonNull Function<T, BigDecimal> decimalValue
  ) {
    return switch (value) {
      case null -> Optional.empty();
      case PointCharacteristicValue<T>(_) -> Optional.empty();
      case IntervalCharacteristicValue<T>(var lower, var upper) -> Optional.of(new ClassificationInterval(
          decimalValue.apply(lower),
          decimalValue.apply(upper)
      ));
    };
  }
}
