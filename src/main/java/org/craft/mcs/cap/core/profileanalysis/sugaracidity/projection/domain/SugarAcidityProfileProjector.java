package org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain;

import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Domain service for projecting a canonical chemical profile onto the sugar-acidity graph.
 *
 * <p>The projector centralizes the shared pipeline used by AppleVariety, Feedstock, ProductionBatch
 * and future VarietyProfile contexts:
 * extract canonical axes, check {@link ChemicalProfileCapability#SUGAR_ACIDITY_PLOT}, then delegate
 * graph-zone membership to the configured {@link SugarAcidityZoneAssessor}. Other capabilities can
 * be checked against the already extracted axes to avoid duplicate extraction work in query flows.
 */
@Service
public final class SugarAcidityProfileProjector {

  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;

  public SugarAcidityProfileProjector(
      @NonNull ChemicalProfileAxisExtractor axisExtractor,
      @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor,
      @NonNull SugarAcidityZoneAssessor zoneAssessor
  ) {
    this.axisExtractor = requireNonNull(axisExtractor, "chemical profile axis extractor cannot be null");
    this.capabilityAssessor = requireNonNull(capabilityAssessor, "chemical profile capability assessor cannot be null");
    this.zoneAssessor = requireNonNull(zoneAssessor, "sugar-acidity zone assessor cannot be null");
  }

  public @NonNull SugarAcidityProfileEvaluation project(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    final var axes = axisExtractor.extract(profile);
    final var capabilityAssessment = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );
    final var assessment = capabilityAssessment.capable()
        ? assessZone(axes)
        : null;

    return new SugarAcidityProfileEvaluation(
        axes,
        capabilityAssessment,
        assessment != null,
        assessment
    );
  }

  public @NonNull Optional<@NonNull SugarAcidityProfilePoint> projectPoint(@NonNull ChemicalProfile profile) {
    return project(profile).point();
  }

  public @NonNull ChemicalProfileCapabilityAssessment assessCapability(
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @NonNull ChemicalProfileCapability capability
  ) {
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
    requireNonNull(capability, "chemical profile capability cannot be null");

    return capabilityAssessor.assess(evaluation.axes(), capability);
  }

  private @Nullable SugarAcidityZoneAssessment assessZone(@NonNull ChemicalProfileAxes axes) {
    final var assessment = zoneAssessor.assess(axes);
    return assessment.orElse(null); // NOSONAR
  }
}
