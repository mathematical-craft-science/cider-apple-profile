package org.craft.mcs.cap.infrastructure.adapter.out.production.measurement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceReference;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView.Characteristic;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView.FreshnessStatus;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProfileView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProvenanceView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProvenanceView.SourceType;
import org.craft.mcs.cap.core.production.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Anti-corruption adapter from generic Measurement projections to Production-owned read models. */
@Component
@RequiredArgsConstructor
final class MeasurementProductionProfileProvider implements ProductionMeasurementProfileProvider {

  private final @NonNull ProjectCurrentMeasurementProfileUseCase measurementProfileProjector;

  @Override
  public @NonNull Optional<@NonNull ProductionMeasurementProfileView> findCurrent(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");
    return measurementProfileProjector.project(subject(batchId))
        .map(MeasurementProductionProfileProvider::toProductionProfile);
  }

  @Override
  public @NonNull Map<@NonNull ProductionBatchId, @NonNull ProductionMeasurementProfileView> findAllCurrent(
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
    return measurementProfileProjector.projectAll(workspaceId, subjects)
        .entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> ProductionBatchId.of(entry.getKey().id()),
            entry -> toProductionProfile(entry.getValue())
        ));
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull ProductionBatchId batchId) {
    return new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        batchId.value()
    );
  }

  private static @NonNull ProductionMeasurementProfileView toProductionProfile(
      @NonNull CurrentMeasurementProfileView profile
  ) {
    return new ProductionMeasurementProfileView(
        profile.chemicalProfile(),
        profile.evidence().stream()
            .map(MeasurementProductionProfileProvider::toProductionEvidence)
            .toList()
    );
  }

  private static @NonNull ProductionMeasurementEvidenceView toProductionEvidence(
      @NonNull MeasurementEvidenceView evidence
  ) {
    final var provenance = evidence.provenance();
    return new ProductionMeasurementEvidenceView(
        characteristic(evidence.characteristic()),
        new ProductionMeasurementEvidenceReference(
            evidence.measurementSetId().value(),
            evidence.recordId().value()
        ),
        evidence.measuredAt(),
        provenance(provenance),
        freshnessStatus(evidence.freshnessStatus())
    );
  }

  private static @NonNull ProductionMeasurementProvenanceView provenance(
      @NonNull MeasurementProvenance provenance
  ) {
    return new ProductionMeasurementProvenanceView(
        sourceType(provenance.sourceType()),
        sourceNameValue(provenance.sourceName()),
        methodValue(provenance.method()),
        referenceValue(provenance.reference()),
        provenance.confidence()
    );
  }

  private static @NonNull SourceType sourceType(@NonNull MeasurementSourceType sourceType) {
    return switch (sourceType) {
      case USER -> SourceType.USER;
      case INSTRUMENT -> SourceType.INSTRUMENT;
      case LAB -> SourceType.LAB;
      case IMPORT -> SourceType.IMPORT;
      case CURATOR -> SourceType.CURATOR;
      case INSTITUTE -> SourceType.INSTITUTE;
      case DERIVED -> SourceType.DERIVED;
    };
  }

  private static @NonNull FreshnessStatus freshnessStatus(
      @NonNull MeasurementFreshnessStatus freshnessStatus
  ) {
    return switch (freshnessStatus) {
      case CURRENT -> FreshnessStatus.CURRENT;
      case STALE -> FreshnessStatus.STALE;
    };
  }

  private static @NonNull Characteristic characteristic(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    return switch (characteristic) {
      case SPECIFIC_GRAVITY -> Characteristic.SUGAR;
      case PH -> Characteristic.PH;
      case TITRATABLE_ACIDITY -> Characteristic.TITRATABLE_ACIDITY;
      case TANNIN -> Characteristic.TANNIN;
      case YAN -> Characteristic.YAN;
      case TEMPERATURE, TURBIDITY -> throw new UnsupportedProductionMeasurementCharacteristicException(
          characteristic
      );
    };
  }
}
