package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Application service for interpreting one apple variety in sugar-acidity graph zones.
 *
 * <p>The service deliberately returns an unplottable result when sugar or acidity is missing. That
 * keeps incomplete catalog data visible to admin/catalog workflows instead of turning partial
 * profiles into server errors.
 */
@Component
@RequiredArgsConstructor
public final class AssessAppleVarietySugarAcidityZoneService
    implements AssessAppleVarietySugarAcidityZoneUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull AppleVarietySugarAcidityZoneResult assess(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));
    final var referenceProfile = variety.referenceProfile();
    final var graphEvaluation = graphEvaluator.evaluate(
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity()
    );

    return new AppleVarietySugarAcidityZoneResult(
        variety.varietyId(),
        variety.name(),
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity(),
        graphEvaluation,
        graphEvaluation.plottable() && referenceProfile.tannin() != null
            ? northAmericanProfileCalculator.assessTannin(referenceProfile.tannin()) // NOSONAR
            : null,
        referenceProfile.specificGravity() == null
            ? null
            : potentialAbvPredictor.predict(
                SpecificGravityValue.of(midpoint(referenceProfile.specificGravity())) // NOSONAR
            )
    );
  }
}
