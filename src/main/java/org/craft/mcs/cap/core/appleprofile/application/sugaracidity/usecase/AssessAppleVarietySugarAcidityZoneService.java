package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for projecting one apple variety onto the sugar-acidity graph zones.
 *
 * <p>The service deliberately returns an unplottable result when sugar or acidity is missing. That
 * keeps incomplete catalog data visible to admin/catalog workflows instead of turning partial
 * profiles into server errors. Zone math and capability checks are delegated to
 * {@link SugarAcidityProfileProjector}.
 */
@Component
@RequiredArgsConstructor
public final class AssessAppleVarietySugarAcidityZoneService
    implements AssessAppleVarietySugarAcidityZoneUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull AppleVarietySugarAcidityZoneResult assess(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));
    final var evaluation = sugarAcidityProfileProjector.project(variety.chemicalProfile());

    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        evaluation.plottable(),
        evaluation.assessment(),
        evaluation.capabilityAssessment()
    );
  }
}
