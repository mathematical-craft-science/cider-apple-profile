package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.capability.mapper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.capability.dto.ProfileAnalysisCapabilityAssessmentResponse;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps specialized analysis input availability to the shared capability transport contract. */
public final class ProfileAnalysisCapabilityRestMapper {

  private ProfileAnalysisCapabilityRestMapper() {
  }

  /** Describes SG/TA graph input availability without reintroducing a universal profile model. */
  public static @NonNull ProfileAnalysisCapabilityAssessmentResponse toResponse(
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation
  ) {
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
    final var presentCharacteristics = EnumSet.of(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementCharacteristic.TITRATABLE_ACIDITY
    );
    if (graphEvaluation.missingCharacteristics().contains(GraphMissingCharacteristic.SPECIFIC_GRAVITY)) {
      presentCharacteristics.remove(MeasurementCharacteristic.SPECIFIC_GRAVITY);
    }
    if (graphEvaluation.missingCharacteristics().contains(GraphMissingCharacteristic.TITRATABLE_ACIDITY)) {
      presentCharacteristics.remove(MeasurementCharacteristic.TITRATABLE_ACIDITY);
    }
    return new ProfileAnalysisCapabilityAssessmentResponse(
        ProfileAnalysisCapability.SUGAR_ACIDITY_GRAPH,
        graphEvaluation.plottable(),
        List.of(MeasurementCharacteristic.SPECIFIC_GRAVITY, MeasurementCharacteristic.TITRATABLE_ACIDITY),
        characteristics(presentCharacteristics),
        characteristics(EnumSet.complementOf(presentCharacteristics).stream()
            .filter(characteristic -> characteristic == MeasurementCharacteristic.SPECIFIC_GRAVITY
                || characteristic == MeasurementCharacteristic.TITRATABLE_ACIDITY)
            .collect(Collectors.toSet()))
    );
  }

  private static @NonNull List<@NonNull MeasurementCharacteristic> characteristics(
      @NonNull Set<@NonNull MeasurementCharacteristic> characteristics
  ) {
    return characteristics.stream()
        .sorted(Comparator.comparing(Enum::ordinal))
        .toList();
  }
}
