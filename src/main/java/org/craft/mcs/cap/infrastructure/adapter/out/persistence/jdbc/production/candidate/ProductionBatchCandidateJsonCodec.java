package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceType;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileDistanceMetric;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetPosition;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;


/** Serializes immutable candidate analysis payloads without leaking JSON into core models. */
@Component
@RequiredArgsConstructor
final class ProductionBatchCandidateJsonCodec {

  private static final TypeReference<List<SourcePayload>> SOURCE_LIST_TYPE = new TypeReference<>() {
  };
  private static final TypeReference<List<CompositionWarning>> WARNING_LIST_TYPE =
      new TypeReference<>() {
      };

  private final @NonNull ObjectMapper objectMapper;

  @NonNull String encodeSources(@NonNull ProductionBatchCandidate candidate) {
    requireNonNull(candidate, "production batch candidate cannot be null");
    final var payload = candidate.sourceComposition().sources().stream()
        .map(source -> new SourcePayload(
            source.reference().type(),
            source.reference().id(),
            source.volume().liters(),
            candidate.evidenceFor(source.reference()).value(),
            candidate.materialStateFor(source.reference()).value()
        ))
        .toList();
    return write(payload);
  }

  @NonNull DecodedSources decodeSources(@NonNull String value) {
    final List<SourcePayload> payload = read(requireNonNull(value, "sources JSON cannot be null"), SOURCE_LIST_TYPE);
    final var components = payload.stream()
        .map(source -> new ProductionSourceComponent(
            new ProductionSourceReference(source.type(), source.id()),
            VolumeValue.ofLiters(source.volumeLiters())
        ))
        .toList();
    final Map<ProductionSourceReference, ProductionCandidateSourceEvidence> stateEvidence = payload.stream()
        .collect(Collectors.toUnmodifiableMap(
            source -> new ProductionSourceReference(source.type(), source.id()),
            source -> new ProductionCandidateSourceEvidence(
                ProductionSourceEvidenceReference.of(source.evidenceReference()),
                MaterialStateId.of(source.materialStateId())
            )
        ));
    return new DecodedSources(ProductionSourceComposition.of(components), stateEvidence);
  }

  @Nullable String encodeTargetDistance(@Nullable ProfileTargetDistance targetDistance) {
    return targetDistance == null ? null : write(TargetDistancePayload.from(targetDistance));
  }

  @Nullable ProfileTargetDistance decodeTargetDistance(@Nullable String value) {
    return value == null ? null : read(value, TargetDistancePayload.class).toDomain();
  }

  @NonNull String encodeWarnings(@NonNull List<@NonNull CompositionWarning> warnings) {
    return write(List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null")));
  }

  @NonNull List<@NonNull CompositionWarning> decodeWarnings(@NonNull String value) {
    return List.copyOf(read(requireNonNull(value, "warnings JSON cannot be null"), WARNING_LIST_TYPE));
  }

  private @NonNull String write(@NonNull Object value) {
    try {
      return objectMapper.writeValueAsString(requireNonNull(value, "JSON value cannot be null"));
    } catch (JacksonException exception) {
      throw new IllegalStateException("cannot serialize production batch candidate", exception);
    }
  }

  private <T extends @NonNull Object> T read(
      @NonNull String value,
      @NonNull Class<T> type
  ) {
    try {
      return objectMapper.readValue(value, type);
    } catch (JacksonException exception) {
      throw new IllegalStateException("cannot deserialize production batch candidate", exception);
    }
  }

  private <T extends @NonNull Object> T read(
      @NonNull String value,
      @NonNull TypeReference<T> type
  ) {
    try {
      return objectMapper.readValue(value, type);
    } catch (JacksonException exception) {
      throw new IllegalStateException("cannot deserialize production batch candidate", exception);
    }
  }

  private record SourcePayload(
      @NonNull ProductionSourceType type,
      @NonNull UUID id,
      @NonNull BigDecimal volumeLiters,
      @NonNull String evidenceReference,
      @NonNull UUID materialStateId
  ) {
  }

  private record TargetDistancePayload(
      @NonNull ProfileTargetDistanceStatus status,
      @NonNull CoordinatePayload point,
      @NonNull CoordinatePayload nearestTargetPoint,
      @NonNull TargetPositionPayload targetPosition,
      @Nullable BigDecimal tannin,
      @Nullable BigDecimal nearestTargetTannin,
      @Nullable BigDecimal value,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
      @NonNull ProfileDistanceMetric metric,
      @NonNull WeightsPayload weights,
      @NonNull SugarAcidityTargetKind targetKind,
      @NonNull SugarAcidityGeometryShape targetShape,
      @NonNull List<@NonNull ProfileEvaluationAxis> missingAxes
  ) {

    private static @NonNull TargetDistancePayload from(
        @NonNull ProfileTargetDistance distance
    ) {
      return new TargetDistancePayload(
          distance.status(),
          CoordinatePayload.from(distance.point()),
          CoordinatePayload.from(distance.nearestTargetPoint()),
          TargetPositionPayload.from(distance.sugarAcidityTargetPosition()),
          decimal(distance.tannin()),
          decimal(distance.nearestTargetTannin()),
          distance.value(),
          distance.insideSugarAcidityTarget(),
          distance.insideTarget(),
          distance.objectiveFunction(),
          distance.metric(),
          WeightsPayload.from(distance.weights()),
          distance.targetKind(),
          distance.targetShape(),
          distance.missingAxes().stream()
              .sorted(Comparator.comparing(Enum::name))
              .toList()
      );
    }

    private @NonNull ProfileTargetDistance toDomain() {
      return new ProfileTargetDistance(
          status,
          point.toDomain(),
          nearestTargetPoint.toDomain(),
          targetPosition.toDomain(),
          tannin(tannin),
          tannin(nearestTargetTannin),
          value,
          insideSugarAcidityTarget,
          insideTarget,
          objectiveFunction,
          metric,
          weights.toDomain(),
          targetKind,
          targetShape,
          Set.copyOf(missingAxes)
      );
    }

    private static @Nullable BigDecimal decimal(@Nullable TanninValue value) {
      return value == null ? null : value.asBigDecimal();
    }

    private static @Nullable TanninValue tannin(@Nullable BigDecimal value) {
      return value == null ? null : TanninValue.of(value);
    }
  }

  private record TargetPositionPayload(
      @NonNull MeasuredValueRangePosition specificGravity,
      @NonNull MeasuredValueRangePosition titratableAcidity
  ) {

    private static @NonNull TargetPositionPayload from(
        @NonNull SugarAcidityTargetPosition position
    ) {
      return new TargetPositionPayload(
          position.specificGravity(),
          position.titratableAcidity()
      );
    }

    private @NonNull SugarAcidityTargetPosition toDomain() {
      return new SugarAcidityTargetPosition(specificGravity, titratableAcidity);
    }
  }

  private record CoordinatePayload(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity
  ) {

    private static @NonNull CoordinatePayload from(
        @NonNull SugarAcidityGraphPoint point
    ) {
      return new CoordinatePayload(
          point.specificGravity().asBigDecimal(),
          point.titratableAcidity().asBigDecimal()
      );
    }

    private @NonNull SugarAcidityGraphPoint toDomain() {
      return new SugarAcidityGraphPoint(
          SpecificGravityValue.of(specificGravity),
          TitratableAcidityValue.of(titratableAcidity)
      );
    }
  }

  private record WeightsPayload(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity,
      @Nullable BigDecimal tannin
  ) {

    private static @NonNull WeightsPayload from(
        @NonNull ProfileTargetObjectiveWeights weights
    ) {
      return new WeightsPayload(
          weights.specificGravity(),
          weights.titratableAcidity(),
          weights.tannin()
      );
    }

    private @NonNull ProfileTargetObjectiveWeights toDomain() {
      return new ProfileTargetObjectiveWeights(specificGravity, titratableAcidity, tannin);
    }
  }

  record DecodedSources(
      @NonNull ProductionSourceComposition composition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
          stateEvidence
  ) {
  }
}
