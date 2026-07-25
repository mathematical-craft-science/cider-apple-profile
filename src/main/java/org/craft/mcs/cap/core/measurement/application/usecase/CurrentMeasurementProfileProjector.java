package org.craft.mcs.cap.core.measurement.application.usecase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialStateView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.projection.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Temporary ChemicalProfile adapter over the authoritative state-aware measurement projection. */
@Component
@RequiredArgsConstructor
public final class CurrentMeasurementProfileProjector
    implements ProjectCurrentMeasurementProfileUseCase {

  private final @NonNull ProjectCurrentMaterialStateUseCase currentMaterialStateProjector;

  @Override
  public @NonNull Optional<@NonNull CurrentMeasurementProfileView> project(
      @NonNull MeasurementSubjectReference subject
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    return toProfile(currentMaterialStateProjector.project(subject));
  }

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMeasurementProfileView> projectAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    final Map<MeasurementSubjectReference, CurrentMeasurementProfileView> profiles = new LinkedHashMap<>();
    currentMaterialStateProjector.projectAll(workspaceId, subjects)
        .forEach((subject, state) -> toProfile(state).ifPresent(profile -> profiles.put(subject, profile)));
    return Map.copyOf(profiles);
  }

  private static @NonNull Optional<@NonNull CurrentMeasurementProfileView> toProfile(
      @NonNull CurrentMaterialStateView state
  ) {
    final var chemicalValues = state.characteristics().stream()
        .filter(value -> MeasurementChemicalProfileProjector.isChemical(value.characteristic()))
        .collect(Collectors.toUnmodifiableMap(
            CurrentMaterialCharacteristicView::characteristic,
            CurrentMaterialCharacteristicView::value
        ));
    return MeasurementChemicalProfileProjector.project(chemicalValues)
        .map(profile -> new CurrentMeasurementProfileView(
            state.currentMaterialStateId(),
            profile,
            state.characteristics().stream()
                .filter(value -> MeasurementChemicalProfileProjector.isChemical(value.characteristic()))
                .map(value -> new MeasurementEvidenceView(
                    value.characteristic(),
                    value.origin(),
                    value.evidenceMaterialStateId(),
                    new MeasurementEvidenceReference(
                        value.measurementSetId().value(),
                        value.recordId().value()
                    ),
                    value.measuredAt(),
                    toPublishedProvenance(value.provenance()),
                    value.freshnessStatus(),
                    value.validity()
                ))
                .toList()
        ));
  }

  private static @NonNull MeasurementProvenanceView toPublishedProvenance(
      @NonNull MeasurementProvenance provenance
  ) {
    return new MeasurementProvenanceView(
        provenance.sourceType(),
        sourceName(provenance.sourceName()),
        method(provenance.method()),
        reference(provenance.reference()),
        provenance.confidence()
    );
  }

  private static @Nullable String sourceName(@Nullable MeasurementSourceName sourceName) {
    return sourceName == null ? null : sourceName.value();
  }

  private static @Nullable String method(@Nullable MeasurementMethod method) {
    return method == null ? null : method.value();
  }

  private static @Nullable String reference(@Nullable MeasurementSourceReference reference) {
    return reference == null ? null : reference.value();
  }
}
