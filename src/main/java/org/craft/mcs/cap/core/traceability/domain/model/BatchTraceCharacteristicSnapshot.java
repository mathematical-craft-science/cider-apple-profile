package org.craft.mcs.cap.core.traceability.domain.model;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Typed characteristic value retained by an immutable material trace snapshot.
 *
 * <p>The sealed variants keep source values and calculated results semantically distinct without
 * encoding both facts in a generic kind flag.</p>
 *
 * @param <T> canonical characteristic value-object type
 */
@ValueObject
public sealed interface BatchTraceCharacteristicSnapshot<T extends MeasuredValue>
    permits BatchTraceCharacteristicSnapshot.Direct, BatchTraceCharacteristicSnapshot.Calculated {

  /** Retains a characteristic supplied by the source material state. */
  static <T extends MeasuredValue> @NonNull BatchTraceCharacteristicSnapshot<T> direct(
      @NonNull CharacteristicValue<T> value
  ) {
    return new Direct<>(value);
  }

  /** Retains one characteristic point calculated for the resulting material state. */
  static <T extends MeasuredValue> @NonNull BatchTraceCharacteristicSnapshot<T> calculated(
      @NonNull T value
  ) {
    return new Calculated<>(value);
  }

  /** Direct point or interval retained from a source material state. */
  @ValueObject
  record Direct<T extends MeasuredValue>(
      @NonNull CharacteristicValue<T> value
  ) implements BatchTraceCharacteristicSnapshot<T> {

    public Direct {
      requireNonNull(value, "direct batch trace characteristic value cannot be null");
    }
  }

  /** Point value calculated for a resulting material state. */
  @ValueObject
  record Calculated<T extends MeasuredValue>(
      @NonNull T value
  ) implements BatchTraceCharacteristicSnapshot<T> {

    public Calculated {
      requireNonNull(value, "calculated batch trace characteristic value cannot be null");
    }
  }
}
