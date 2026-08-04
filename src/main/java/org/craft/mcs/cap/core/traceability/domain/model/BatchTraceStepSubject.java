package org.craft.mcs.cap.core.traceability.domain.model;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * References one immutable material state participating in a trace step.
 *
 * <p>{@link #materialStateId()} identifies the historical state, while {@link #subject()} identifies
 * the stable business material. An output snapshot defines the produced state. An input snapshot
 * describes the portion and measured profile participating in that particular step; it does not
 * redefine the referenced state. {@link #invalidatedCharacteristics()} records measurement
 * evidence that must not cross this output-state boundary.
 * </p>
 */
@ValueObject
public record BatchTraceStepSubject(
    @NonNull MaterialStateId materialStateId,
    @NonNull BatchTraceSubject subject,
    @Nullable BatchTraceMaterialSnapshot snapshot,
    @NonNull Set<@NonNull MeasurementCharacteristic> invalidatedCharacteristics
) {

  public BatchTraceStepSubject {
    requireNonNull(materialStateId, "material state id cannot be null");
    requireNonNull(subject, "batch trace step subject cannot be null");
    invalidatedCharacteristics = Set.copyOf(requireNonNull(
        invalidatedCharacteristics,
        "invalidated measurement characteristics cannot be null"
    ));
  }

  public BatchTraceStepSubject(
      @NonNull MaterialStateId materialStateId,
      @NonNull BatchTraceSubject subject,
      @Nullable BatchTraceMaterialSnapshot snapshot
  ) {
    this(materialStateId, subject, snapshot, Set.of());
  }

  public static @NonNull BatchTraceStepSubject reference(
      @NonNull MaterialStateId materialStateId,
      @NonNull BatchTraceSubject subject
  ) {
    return new BatchTraceStepSubject(materialStateId, subject, null, Set.of());
  }

  /** Captures a supplied immutable material state; the owning aggregate creates its identity. */
  public static @NonNull BatchTraceStepSubject capture(
      @NonNull MaterialStateId materialStateId,
      @NonNull BatchTraceSubject subject,
      @NonNull BatchTraceMaterialSnapshot snapshot
  ) {
    return new BatchTraceStepSubject(
        materialStateId,
        subject,
        snapshot,
        Set.of()
    );
  }
}
