package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.application.compatibility.ChemicalProfileAxesPotentialAbvAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for interpreting one apple variety in sugar-acidity graph zones.
 *
 * <p>The service deliberately returns an unplottable result when sugar or acidity is missing. That
 * keeps incomplete catalog data visible to admin/catalog workflows instead of turning partial
 * profiles into server errors. Zone math and capability checks are delegated to
 * {@link ChemicalProfileAssessmentAdapter}.
 */
@Component
@RequiredArgsConstructor
public final class AssessAppleVarietySugarAcidityZoneService
    implements AssessAppleVarietySugarAcidityZoneUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull AppleVarietySugarAcidityZoneResult assess(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));
    final var evaluation = chemicalProfileAssessmentAdapter.project(variety.chemicalProfile());

    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        evaluation,
        ChemicalProfileAxesPotentialAbvAdapter.predict(
            evaluation.axes(),
            potentialAbvPredictor
        ).orElse(null) // NOSONAR
    );
  }
}
