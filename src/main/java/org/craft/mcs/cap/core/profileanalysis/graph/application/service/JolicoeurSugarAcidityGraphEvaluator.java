package org.craft.mcs.cap.core.profileanalysis.graph.application.service;

import java.util.EnumSet;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Builds typed SG/TA graph evidence and applies the Jolicoeur zone system.
 */
public final class JolicoeurSugarAcidityGraphEvaluator {

  private final @NonNull GraphRepresentativeValuePolicy representativeValuePolicy;
  private final @NonNull JolicoeurSugarAcidityZoneAssessor zoneAssessor;

  public JolicoeurSugarAcidityGraphEvaluator(
      @NonNull GraphRepresentativeValuePolicy representativeValuePolicy,
      @NonNull JolicoeurSugarAcidityZoneAssessor zoneAssessor
  ) {
    this.representativeValuePolicy = requireNonNull(
        representativeValuePolicy,
        "graph representative value policy cannot be null"
    );
    this.zoneAssessor = requireNonNull(zoneAssessor, "sugar-acidity zone assessor cannot be null");
  }

  public @NonNull JolicoeurSugarAcidityGraphEvaluation evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    final var missing = missingCharacteristics(specificGravity, titratableAcidity);
    if (!missing.isEmpty()) {
      return JolicoeurSugarAcidityGraphEvaluation.unplottable(missing);
    }

    final var point = new SugarAcidityGraphPoint(
        representativeValuePolicy.specificGravity(requireNonNull(specificGravity)),
        representativeValuePolicy.titratableAcidity(requireNonNull(titratableAcidity))
    );
    return JolicoeurSugarAcidityGraphEvaluation.plottable(
        point,
        zoneAssessor.assess(point)
    );
  }

  private static @NonNull EnumSet<GraphMissingCharacteristic> missingCharacteristics(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    final var missing = EnumSet.noneOf(GraphMissingCharacteristic.class);
    if (specificGravity == null) {
      missing.add(GraphMissingCharacteristic.SPECIFIC_GRAVITY);
    }
    if (titratableAcidity == null) {
      missing.add(GraphMissingCharacteristic.TITRATABLE_ACIDITY);
    }
    return missing;
  }
}
