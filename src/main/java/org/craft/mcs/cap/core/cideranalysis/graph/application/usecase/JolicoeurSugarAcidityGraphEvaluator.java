package org.craft.mcs.cap.core.cideranalysis.graph.application.usecase;

import java.util.EnumSet;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation.MissingCharacteristic;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.GraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Builds typed SG/TA graph evidence and applies the Jolicoeur zone system.
 */
public final class JolicoeurSugarAcidityGraphEvaluator
    implements EvaluateJolicoeurSugarAcidityGraphUseCase {

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

  @Override
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
        new JolicoeurSugarAcidityGraphEvaluation.Point(
            point.specificGravity(),
            point.titratableAcidity()
        ),
        zoneAssessment(zoneAssessor.assess(point))
    );
  }

  private static @NonNull EnumSet<MissingCharacteristic> missingCharacteristics(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  ) {
    final var missing = EnumSet.noneOf(MissingCharacteristic.class);
    if (specificGravity == null) {
      missing.add(MissingCharacteristic.SPECIFIC_GRAVITY);
    }
    if (titratableAcidity == null) {
      missing.add(MissingCharacteristic.TITRATABLE_ACIDITY);
    }
    return missing;
  }

  private static JolicoeurSugarAcidityGraphEvaluation.@NonNull ZoneAssessment zoneAssessment(
      @NonNull JolicoeurSugarAcidityZoneAssessment assessment
  ) {
    final var zone = assessment.zone();
    return new JolicoeurSugarAcidityGraphEvaluation.ZoneAssessment(
        zone == null ? null : JolicoeurSugarAcidityZoneCode.valueOf(zone.name())
    );
  }
}
