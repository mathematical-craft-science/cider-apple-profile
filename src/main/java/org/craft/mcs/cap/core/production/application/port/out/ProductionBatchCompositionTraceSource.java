package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Production trace source for an accepted composition/calculation result.
 */
public record ProductionBatchCompositionTraceSource(
    @NonNull String compositionResultId,
    @Nullable String displayName,
    @NonNull VolumeValue volume,
    @Nullable ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence
) implements ProductionBatchTraceSource {

  public ProductionBatchCompositionTraceSource(
      @NonNull String compositionResultId,
      @Nullable String displayName,
      @NonNull VolumeValue volume
  ) {
    this(compositionResultId, displayName, volume, null, null);
  }

  public ProductionBatchCompositionTraceSource {
    compositionResultId = requireNonNull(compositionResultId, "composition result id cannot be null").strip();
    displayName = normalize(displayName);
    requireNonNull(volume, "production trace source volume cannot be null");

    if (compositionResultId.isEmpty()) {
      throw new ApplicationValidationException("composition result id cannot be blank");
    }
    if (volume.isZero()) {
      throw new ApplicationValidationException("production trace source volume must be positive");
    }
  }

  @Override
  public @NonNull String id() {
    return compositionResultId;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
