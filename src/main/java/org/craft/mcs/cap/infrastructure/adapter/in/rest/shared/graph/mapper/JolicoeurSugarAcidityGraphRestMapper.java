package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.GraphMissingCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone;
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
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
    requireNonNull(specificGravity, "graph specific gravity cannot be null");
    requireNonNull(titratableAcidity, "graph titratable acidity cannot be null");
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
    final var zone = zoneAssessment.zone();
    return new SugarAcidityGraphAssessmentResponse(
        zone == null ? null : JolicoeurSugarAcidityZone.valueOf(zone.name()),
        northAmericanTanninLevel,
        toResponse(specificGravity, titratableAcidity)
    );
  }

  public static @NonNull SugarAcidityGraphAssessmentResponse toResponse(
      @NonNull FeedstockSugarAcidityGraphView graphEvaluation,
      @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    requireNonNull(graphEvaluation, "feedstock sugar-acidity graph evaluation cannot be null");
    final var zoneAssessment = requireNonNull(
        graphEvaluation.zoneAssessment(),
        "plottable feedstock graph evaluation must contain zone assessment"
    );
    final var zone = zoneAssessment.zone();
    return new SugarAcidityGraphAssessmentResponse(
        zone == null ? null : JolicoeurSugarAcidityZone.valueOf(zone.name()),
        northAmericanTanninLevel,
        toResponse(specificGravity, titratableAcidity)
    );
  }

  public static @NonNull SugarAcidityGraphAssessmentResponse toResponse(
      @NonNull AppleSugarAcidityGraphView graphEvaluation,
      @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    requireNonNull(graphEvaluation, "apple sugar-acidity graph evaluation cannot be null");
    final var zoneAssessment = requireNonNull(
        graphEvaluation.zoneAssessment(),
        "plottable apple graph evaluation must contain zone assessment"
    );
    final var zone = zoneAssessment.zone();
    return new SugarAcidityGraphAssessmentResponse(
        zone == null ? null : JolicoeurSugarAcidityZone.valueOf(zone.name()),
        northAmericanTanninLevel,
        toResponse(specificGravity, titratableAcidity)
    );
  }

  public static @NonNull SugarAcidityGraphAssessmentResponse toResponse(
      @NonNull AppleSugarAcidityGraphView graphEvaluation,
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    return toResponse(graphEvaluation, null, specificGravity, titratableAcidity);
  }

  public static @NonNull SugarAcidityGraphPointResponse toResponse(
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    requireNonNull(specificGravity, "graph specific gravity cannot be null");
    requireNonNull(titratableAcidity, "graph titratable acidity cannot be null");
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
        .map(characteristic -> GraphMissingCharacteristic.valueOf(characteristic.name()))
        .sorted()
        .toList();
  }

  public static @NonNull List<@NonNull GraphMissingCharacteristic> missingCharacteristics(
      @NonNull FeedstockSugarAcidityGraphView graphEvaluation
  ) {
    requireNonNull(graphEvaluation, "feedstock sugar-acidity graph evaluation cannot be null");
    return graphEvaluation.missingCharacteristics().stream()
        .map(characteristic -> GraphMissingCharacteristic.valueOf(characteristic.name()))
        .sorted()
        .toList();
  }

  public static @NonNull List<@NonNull GraphMissingCharacteristic> missingCharacteristics(
      @NonNull AppleSugarAcidityGraphView graphEvaluation
  ) {
    requireNonNull(graphEvaluation, "apple sugar-acidity graph evaluation cannot be null");
    return graphEvaluation.missingCharacteristics().stream()
        .map(characteristic -> GraphMissingCharacteristic.valueOf(characteristic.name()))
        .sorted()
        .toList();
  }
}
