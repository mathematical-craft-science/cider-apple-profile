package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@ValueObject
public record BatchTraceMaterialSnapshot(
    @Nullable VolumeValue volume,
    @Nullable ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence
) {

  public BatchTraceMaterialSnapshot {
    if (volume == null && profile == null && confidence == null) {
      throw new DomainRuleViolationException("batch trace material snapshot must contain at least one value");
    }
  }

  public static @NonNull BatchTraceMaterialSnapshot ofVolume(@NonNull VolumeValue volume) {
    return new BatchTraceMaterialSnapshot(volume, null, null);
  }
}
