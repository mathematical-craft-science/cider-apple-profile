package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps sugar-acidity graph projections and assessments to transport responses. */
public final class ProfileGraphRestMapper {

  private ProfileGraphRestMapper() {
  }

  public static @NonNull SugarAcidityGraphAssessmentResponse toResponse(
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
    final var assessment = requireNonNull(
        evaluation.assessment(),
        "plottable sugar-acidity evaluation must contain zone assessment"
    );
    return new SugarAcidityGraphAssessmentResponse(
        assessment.zone(),
        evaluation.northAmericanTanninLevel(),
        toResponse(evaluation.axes())
    );
  }

  public static @NonNull SugarAcidityGraphPointResponse toResponse(
      @NonNull ChemicalProfileAxes axes
  ) {
    requireNonNull(axes, "chemical profile axes cannot be null");

    return new SugarAcidityGraphPointResponse(
        ProfileCharacteristicRestMapper.toResponse(
            axes.requireValueOf(ChemicalAxis.SPECIFIC_GRAVITY),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        ProfileCharacteristicRestMapper.toResponse(
            axes.requireValueOf(ChemicalAxis.TITRATABLE_ACIDITY),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        )
    );
  }
}
