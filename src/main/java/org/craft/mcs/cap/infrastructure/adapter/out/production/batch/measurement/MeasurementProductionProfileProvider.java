package org.craft.mcs.cap.infrastructure.adapter.out.production.batch.measurement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryCurrentMeasurementEvidenceUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionCurrentCharacteristicView;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Anti-corruption adapter from generic Measurement projections to Production-owned read models. */
@Component
@RequiredArgsConstructor
final class MeasurementProductionProfileProvider implements ProductionMeasurementProfileProvider {

  private final @NonNull QueryCurrentMeasurementEvidenceUseCase currentMeasurementEvidenceQuery;

  @Override
  public @NonNull Optional<@NonNull ProductionMeasuredProfileView> findCurrent(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");
    return toProductionProfile(currentMeasurementEvidenceQuery.getCurrent(subject(batchId)));
  }

  @Override
  public @NonNull Map<@NonNull ProductionBatchId, @NonNull ProductionMeasuredProfileView> findAllCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "production workspace id cannot be null");
    requireNonNull(batchIds, "production batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return Map.of();
    }
    final var subjects = batchIds.stream()
        .map(MeasurementProductionProfileProvider::subject)
        .collect(Collectors.toUnmodifiableSet());
    return currentMeasurementEvidenceQuery.getAllCurrent(workspaceId, subjects)
        .entrySet()
        .stream()
        .map(entry -> toProductionProfile(entry.getValue())
            .map(profile -> Map.entry(ProductionBatchId.of(entry.getKey().id()), profile)))
        .flatMap(Optional::stream)
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull ProductionBatchId batchId) {
    return new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        batchId.value()
    );
  }

  private static @NonNull Optional<@NonNull ProductionMeasuredProfileView> toProductionProfile(
      @NonNull CurrentMeasurementEvidenceView currentEvidence
  ) {
    final var chemicalEvidence = currentEvidence.characteristics().stream()
        .filter(characteristic -> switch (characteristic.characteristic()) {
          case SPECIFIC_GRAVITY, PH, TITRATABLE_ACIDITY, TANNIN, YAN -> true;
          case TEMPERATURE, TURBIDITY -> false;
        })
        .toList();
    if (chemicalEvidence.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new ProductionMeasuredProfileView(
        measured(currentEvidence, MeasurementCharacteristic.SPECIFIC_GRAVITY, SpecificGravityValue.class),
        measured(currentEvidence, MeasurementCharacteristic.TITRATABLE_ACIDITY, TitratableAcidityValue.class),
        measured(currentEvidence, MeasurementCharacteristic.PH, PhValue.class),
        measured(currentEvidence, MeasurementCharacteristic.TANNIN, TanninValue.class),
        measured(currentEvidence, MeasurementCharacteristic.YAN, YeastAssimilableNitrogenValue.class)
    ));
  }

  private static <T extends MeasuredValue> @Nullable ProductionCurrentCharacteristicView<T> measured(
      @NonNull CurrentMeasurementEvidenceView currentEvidence,
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull Class<T> valueType
  ) {
    final var evidence = currentEvidence.characteristic(characteristic).orElse(null); // NOSONAR
    if (evidence == null) {
      return null;
    }
    final var value = currentEvidence.value(characteristic, valueType).orElseThrow();
    return ProductionCurrentCharacteristicView.fromMeasurement(value, evidence.evidence());
  }
}
