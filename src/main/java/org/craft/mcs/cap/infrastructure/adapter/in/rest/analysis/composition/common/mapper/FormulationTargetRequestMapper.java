package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Maps transport target specifications to the Formulation published target language. */
public final class FormulationTargetRequestMapper {

  private FormulationTargetRequestMapper() {
  }

  public static @Nullable FormulationTargetDefinition toTarget(
      @Nullable ProfileTargetRequest target,
      @Nullable ProfileTargetTanninRangeRequest tannin,
      @Nullable ProfileTargetObjectiveWeightsRequest weights,
      @NonNull ProfileAxisNormalizerCode normalizer,
      @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction
  ) {
    if (target == null) {
      return null;
    }
    final FormulationTargetDefinition.TargetArea targetArea = switch (target) {
      case PredefinedZoneProfileTargetRequest(_, var zone) -> new FormulationTargetDefinition.PredefinedTarget(
          FormulationTargetDefinition.Zone.valueOf(zone.name())
      );
      case CustomRectangleProfileTargetRequest rectangle -> new FormulationTargetDefinition.CustomRectangleTarget(
          MeasuredValueRange.closed(
              SpecificGravityValue.of(rectangle.specificGravityMin()),
              SpecificGravityValue.of(rectangle.specificGravityMax())
          ),
          MeasuredValueRange.closed(
              TitratableAcidityValue.of(rectangle.titratableAcidityMin()),
              TitratableAcidityValue.of(rectangle.titratableAcidityMax())
          )
      );
    };
    final var requestedSpecificGravityWeight = weights == null ? null : weights.specificGravity();
    final var requestedTitratableAcidityWeight = weights == null ? null : weights.titratableAcidity();
    final var specificGravityWeight = requestedSpecificGravityWeight == null
        ? BigDecimal.ONE
        : requestedSpecificGravityWeight;
    final var titratableAcidityWeight = requestedTitratableAcidityWeight == null
        ? BigDecimal.ONE
        : requestedTitratableAcidityWeight;
    return new FormulationTargetDefinition(
        targetArea,
        tannin == null
            ? null
            : MeasuredValueRange.closed(TanninValue.of(tannin.min()), TanninValue.of(tannin.max())),
        new FormulationTargetDefinition.ScoringConfiguration(
            FormulationTargetDefinition.Normalizer.valueOf(normalizer.name()),
            FormulationTargetDefinition.ObjectiveFunction.valueOf(objectiveFunction.name()),
            new FormulationTargetDefinition.Weights(
                specificGravityWeight,
                titratableAcidityWeight,
                weights == null ? null : weights.tannin()
            )
        )
    );
  }
}
