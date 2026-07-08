package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileCapabilityRestMapper {

  private ChemicalProfileCapabilityRestMapper() {
  }

  public static @NonNull ChemicalProfileCapabilityAssessmentResponse toResponse(
      @NonNull ChemicalProfileCapabilityAssessment assessment
  ) {
    requireNonNull(assessment, "chemical profile capability assessment cannot be null");

    return new ChemicalProfileCapabilityAssessmentResponse(
        assessment.capability(),
        assessment.capable(),
        axes(assessment.requiredAxes()),
        axes(assessment.presentAxes()),
        axes(assessment.missingAxes())
    );
  }

  private static @NonNull List<@NonNull ChemicalAxis> axes(
      @NonNull Set<@NonNull ChemicalAxis> axes
  ) {
    return axes.stream()
        .sorted(Comparator.comparing(Enum::ordinal))
        .toList();
  }
}
