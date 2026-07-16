package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceType;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
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
  private static final TypeReference<List<ChemicalProfileCompositionWarning>> WARNING_LIST_TYPE =
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
            candidate.evidenceFor(source.reference()).value()
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
    final Map<ProductionSourceReference, ProductionSourceEvidenceReference> evidence = payload.stream()
        .collect(Collectors.toUnmodifiableMap(
            source -> new ProductionSourceReference(source.type(), source.id()),
            source -> ProductionSourceEvidenceReference.of(source.evidenceReference())
        ));
    return new DecodedSources(ProductionSourceComposition.of(components), evidence);
  }

  @Nullable String encodeTargetDistance(@Nullable ProfileTargetDistance targetDistance) {
    return targetDistance == null ? null : write(targetDistance);
  }

  @Nullable ProfileTargetDistance decodeTargetDistance(@Nullable String value) {
    return value == null ? null : read(value, ProfileTargetDistance.class);
  }

  @NonNull String encodeWarnings(@NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings) {
    return write(List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null")));
  }

  @NonNull List<@NonNull ChemicalProfileCompositionWarning> decodeWarnings(@NonNull String value) {
    return List.copyOf(read(requireNonNull(value, "warnings JSON cannot be null"), WARNING_LIST_TYPE));
  }

  private @NonNull String write(@NonNull Object value) {
    try {
      return objectMapper.writeValueAsString(requireNonNull(value, "JSON value cannot be null"));
    } catch (JacksonException exception) {
      throw new IllegalStateException("cannot serialize production batch candidate", exception);
    }
  }

  private <T extends @NonNull Object> @NonNull T read(
      @NonNull String value,
      @NonNull Class<T> type
  ) {
    try {
      return objectMapper.readValue(value, type);
    } catch (JacksonException exception) {
      throw new IllegalStateException("cannot deserialize production batch candidate", exception);
    }
  }

  private <T extends @NonNull Object> @NonNull T read(
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
      @NonNull String evidenceReference
  ) {
  }

  record DecodedSources(
      @NonNull ProductionSourceComposition composition,
      @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference> evidence
  ) {
  }
}
