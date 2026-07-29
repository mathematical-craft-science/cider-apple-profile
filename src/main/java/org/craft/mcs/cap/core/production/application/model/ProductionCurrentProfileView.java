package org.craft.mcs.cap.core.production.application.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Current production profile obtained by overlaying measured axes on the accepted initial snapshot.
 */
public record ProductionCurrentProfileView(
    @NonNull ProductionCurrentCharacteristicView<SpecificGravityValue> specificGravity,
    @NonNull ProductionCurrentCharacteristicView<TitratableAcidityValue> titratableAcidity,
    @Nullable ProductionCurrentCharacteristicView<PhValue> ph,
    @Nullable ProductionCurrentCharacteristicView<TanninValue> tannin,
    @Nullable ProductionCurrentCharacteristicView<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public ProductionCurrentProfileView {
    requireNonNull(specificGravity, "production current specific gravity cannot be null");
    requireNonNull(titratableAcidity, "production current titratable acidity cannot be null");
  }

  /** Returns only measurement evidence; initial-snapshot characteristics have no measurement evidence. */
  public @NonNull List<@NonNull MeasurementEvidenceView> measurementEvidence() {
    return Stream.of(
            specificGravity,
            titratableAcidity,
            ph,
            tannin,
            yeastAssimilableNitrogen
        )
        .filter(Objects::nonNull)
        .map(ProductionCurrentCharacteristicView::evidence)
        .filter(Objects::nonNull)
        .toList();
  }
}
