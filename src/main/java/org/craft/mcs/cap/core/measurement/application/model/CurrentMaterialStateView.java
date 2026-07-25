package org.craft.mcs.cap.core.measurement.application.model;

import java.util.List;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** State-aware projection of currently applicable measured characteristics for one material subject. */
public record CurrentMaterialStateView(
    @NonNull MeasurementSubjectReference subject,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull List<@NonNull CurrentMaterialCharacteristicView> characteristics
) {

  public CurrentMaterialStateView {
    requireNonNull(subject, "current material subject cannot be null");
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    characteristics = List.copyOf(
        requireNonNull(characteristics, "current material characteristics cannot be null")
    );
  }
}
