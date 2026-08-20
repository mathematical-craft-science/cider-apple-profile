package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.planning;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceType;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest.ObjectiveFunction;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.AxisPosition;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.Coordinates;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.EvaluationAxis;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.FitStatus;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.GeometryShape;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.Metric;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.Status;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.TargetKind;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.TargetPosition;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView.Weights;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateWarning;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataRetrievalFailureException;
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
  private static final TypeReference<List<ProductionCandidateWarning>> WARNING_LIST_TYPE =
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

  @Nullable String encodeTargetDistance(@Nullable ProductionCandidateTargetDistanceView targetDistance) {
    return targetDistance == null ? null : write(TargetDistancePayload.from(targetDistance));
  }

  @Nullable ProductionCandidateTargetDistanceView decodeTargetDistance(@Nullable String value) {
    if (value == null) {
      return null;
    }
    try {
      return read(value, TargetDistancePayload.class).toView();
    } catch (DataRetrievalFailureException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new DataRetrievalFailureException("persisted production target distance is invalid", exception);
    }
  }

  @NonNull String encodeWarnings(@NonNull List<@NonNull ProductionCandidateWarning> warnings) {
    return write(List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null")));
  }

  @NonNull List<@NonNull ProductionCandidateWarning> decodeWarnings(@NonNull String value) {
    return List.copyOf(read(requireNonNull(value, "warnings JSON cannot be null"), WARNING_LIST_TYPE));
  }

  private @NonNull String write(@NonNull Object value) {
    try {
      return objectMapper.writeValueAsString(requireNonNull(value, "JSON value cannot be null"));
    } catch (JacksonException exception) {
      throw new DataRetrievalFailureException("cannot serialize production batch candidate", exception);
    }
  }

  private <T extends @NonNull Object> T read(
      @NonNull String value,
      @NonNull Class<T> type
  ) {
    try {
      return objectMapper.readValue(value, type);
    } catch (JacksonException exception) {
      throw new DataRetrievalFailureException("cannot deserialize production batch candidate", exception);
    }
  }

  private <T extends @NonNull Object> T read(
      @NonNull String value,
      @NonNull TypeReference<T> type
  ) {
    try {
      return objectMapper.readValue(value, type);
    } catch (JacksonException exception) {
      throw new DataRetrievalFailureException("cannot deserialize production batch candidate", exception);
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
      @NonNull Status status,
      @NonNull CoordinatePayload point,
      @NonNull CoordinatePayload nearestTargetPoint,
      @NonNull TargetPositionPayload targetPosition,
      @Nullable BigDecimal tannin,
      @Nullable BigDecimal nearestTargetTannin,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      @Nullable FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull ObjectiveFunction objectiveFunction,
      @NonNull Metric metric,
      @NonNull WeightsPayload weights,
      @NonNull TargetKind targetKind,
      @NonNull GeometryShape targetShape,
      @NonNull List<@NonNull EvaluationAxis> missingAxes
  ) {

    private static @NonNull TargetDistancePayload from(
        @NonNull ProductionCandidateTargetDistanceView distance
    ) {
      return new TargetDistancePayload(
          distance.status(),
          CoordinatePayload.from(distance.point()),
          CoordinatePayload.from(distance.nearestTargetPoint()),
          TargetPositionPayload.from(distance.targetPosition()),
          decimal(distance.tannin()),
          decimal(distance.nearestTargetTannin()),
          distance.value(),
          distance.matchPercent(),
          distance.fitStatus(),
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

    private @NonNull ProductionCandidateTargetDistanceView toView() {
      final var resolvedFit = resolvedFit();
      return new ProductionCandidateTargetDistanceView(
          status,
          point.toView(),
          nearestTargetPoint.toView(),
          targetPosition.toView(),
          tannin(tannin),
          tannin(nearestTargetTannin),
          value,
          resolvedFit.matchPercent(),
          resolvedFit.status(),
          insideSugarAcidityTarget,
          insideTarget,
          objectiveFunction,
          metric,
          weights.toView(),
          targetKind,
          targetShape,
          Set.copyOf(missingAxes)
      );
    }

    private @NonNull ProductionTargetFit resolvedFit() {
      if (fitStatus == null) {
        if (matchPercent != null) {
          throw new DataRetrievalFailureException("persisted production target fit is incomplete");
        }
        return legacyFit(value);
      }
      return new ProductionTargetFit(matchPercent, fitStatus);
    }

    private @NonNull ProductionTargetFit legacyFit(@Nullable BigDecimal distance) {
      if (status == Status.UNSCORABLE || distance == null) {
        return new ProductionTargetFit(null, FitStatus.UNSCORABLE);
      }
      if (insideTarget) {
        return new ProductionTargetFit(100, FitStatus.EXCELLENT);
      }
      final var percent = BigDecimal.valueOf(100)
          .divide(BigDecimal.ONE.add(distance), 0, java.math.RoundingMode.HALF_UP)
          .intValueExact();
      final var cappedPercent = Math.min(percent, 99);
      final var fitStatus = cappedPercent >= 90
          ? FitStatus.EXCELLENT
          : cappedPercent >= 70
              ? FitStatus.CLOSE
              : FitStatus.NEEDS_ADJUSTMENT;
      return new ProductionTargetFit(cappedPercent, fitStatus);
    }

    private static @Nullable BigDecimal decimal(@Nullable TanninValue value) {
      return value == null ? null : value.asBigDecimal();
    }

    private static @Nullable TanninValue tannin(@Nullable BigDecimal value) {
      return value == null ? null : TanninValue.of(value);
    }
  }

  private record TargetPositionPayload(
      @NonNull AxisPosition specificGravity,
      @NonNull AxisPosition titratableAcidity
  ) {

    private static @NonNull TargetPositionPayload from(
        @NonNull TargetPosition position
    ) {
      return new TargetPositionPayload(
          position.specificGravity(),
          position.titratableAcidity()
      );
    }

    private @NonNull TargetPosition toView() {
      return new TargetPosition(specificGravity, titratableAcidity);
    }
  }

  private record CoordinatePayload(
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidity
  ) {

    private static @NonNull CoordinatePayload from(
        @NonNull Coordinates point
    ) {
      return new CoordinatePayload(
          point.specificGravity().asBigDecimal(),
          point.titratableAcidity().asBigDecimal()
      );
    }

    private @NonNull Coordinates toView() {
      return new Coordinates(
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
        @NonNull Weights weights
    ) {
      return new WeightsPayload(
          weights.specificGravity(),
          weights.titratableAcidity(),
          weights.tannin()
      );
    }

    private @NonNull Weights toView() {
      return new Weights(specificGravity, titratableAcidity, tannin);
    }
  }

  private record ProductionTargetFit(
      @Nullable Integer matchPercent,
      @NonNull FitStatus status
  ) {
  }

  record DecodedSources(
      @NonNull ProductionSourceComposition composition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
          stateEvidence
  ) {
  }
}
