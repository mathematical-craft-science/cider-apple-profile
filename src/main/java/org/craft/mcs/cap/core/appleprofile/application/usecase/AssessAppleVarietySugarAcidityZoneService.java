package org.craft.mcs.cap.core.appleprofile.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for projecting one apple variety onto the sugar-acidity graph zones.
 *
 * <p>The service deliberately returns an unplottable result when sugar or acidity is missing. That
 * keeps incomplete catalog data visible to admin/catalog workflows instead of turning partial
 * profiles into server errors. Zone math is delegated to the configured
 * {@link SugarAcidityZoneAssessor}.
 */
@Component
@RequiredArgsConstructor
public final class AssessAppleVarietySugarAcidityZoneService
    implements AssessAppleVarietySugarAcidityZoneUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull AppleVarietySugarAcidityZoneResult assess(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new IllegalArgumentException("apple variety not found: " + varietyId.value()));
    final var axes = axisExtractor.extract(variety.chemicalProfile());
    final var capability = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    if (!capability.capable()) {
      return new AppleVarietySugarAcidityZoneResult(
          variety.varietyId(),
          variety.name(),
          false,
          null,
          capability
      );
    }

    final var assessment = assessor.assess(axes)
        .orElseThrow(() -> new IllegalStateException(
            "sugar-acidity assessor did not assess capable apple variety profile: " + varietyId.value()
        ));

    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        true,
        assessment,
        capability
    );
  }
}
