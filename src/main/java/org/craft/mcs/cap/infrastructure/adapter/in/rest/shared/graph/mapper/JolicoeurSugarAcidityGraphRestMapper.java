package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import java.util.Comparator;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps sugar-acidity graph projections and assessments to transport responses. */
public final class JolicoeurSugarAcidityGraphRestMapper {

  private JolicoeurSugarAcidityGraphRestMapper() {
  }

  public static @NonNull SugarAcidityGraphAssessmentResponse toResponse(
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    return toResponse(
        graphEvaluation,
        null,
        specificGravity,
        titratableAcidity
    );
  }

  public static @NonNull SugarAcidityGraphAssessmentResponse toResponse(
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
      @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
    final var zoneAssessment = requireNonNull(
        graphEvaluation.zoneAssessment(),
        "plottable sugar-acidity graph evaluation must contain zone assessment"
    );
    return new SugarAcidityGraphAssessmentResponse(
        zoneAssessment.zone(),
        northAmericanTanninLevel,
        toResponse(specificGravity, titratableAcidity)
    );
  }

  public static @NonNull SugarAcidityGraphPointResponse toResponse(
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    return new SugarAcidityGraphPointResponse(
        ProfileCharacteristicRestMapper.toResponse(
            specificGravity,
            SpecificGravityValue::asBigDecimal,
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        ProfileCharacteristicRestMapper.toResponse(
            titratableAcidity,
            TitratableAcidityValue::asBigDecimal,
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        )
    );
  }

  /** Returns graph-specific missing inputs in deterministic enum order. */
  public static @NonNull List<@NonNull GraphMissingCharacteristic> missingCharacteristics(
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation
  ) {
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
    return graphEvaluation.missingCharacteristics().stream()
        .sorted(Comparator.comparingInt(Enum::ordinal))
        .toList();
  }
}
