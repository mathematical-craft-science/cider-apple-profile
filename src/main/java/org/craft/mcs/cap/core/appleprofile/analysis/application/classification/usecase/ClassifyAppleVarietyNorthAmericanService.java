package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.usecase;

import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.AppleVarietyNorthAmericanProfileUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in.ClassifyAppleVarietyNorthAmericanUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for the North American profile projection of a catalog variety.
 *
 * <p>Unlike generic classification systems, the North American calculator is a reusable profile
 * calculation component that can later participate in other algorithms. This service therefore
 * performs the use-case specific steps around it: load the variety and require the typed
 * characteristics needed by the North American profile.
 */
@Component
@RequiredArgsConstructor
public final class ClassifyAppleVarietyNorthAmericanService
    implements ClassifyAppleVarietyNorthAmericanUseCase {

  private final @NonNull AppleVarietyAnalysisProfileProvider profileProvider;
  private final @NonNull NorthAmericanProfileCalculator calculator;

  @Override
  public @NonNull AppleVarietyNorthAmericanClassification classify(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = profileProvider.get(varietyId);
    if (variety.specificGravity() == null
        || variety.titratableAcidity() == null
        || variety.tannin() == null) {
      throw new AppleVarietyNorthAmericanProfileUnavailableException(
          varietyId,
          presentCharacteristics(variety)
      );
    }

    final var profile = calculator.calculate(
        new CiderAppleClassificationInput(
            variety.specificGravity(),
            variety.titratableAcidity(),
            variety.tannin()
        )
    );

    return new AppleVarietyNorthAmericanClassification(
        variety.varietyId(),
        variety.name(),
        profile
    );
  }

  private static @NonNull EnumSet<MeasurementCharacteristic> presentCharacteristics(
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    final var present = EnumSet.noneOf(MeasurementCharacteristic.class);
    if (profile.specificGravity() != null) {
      present.add(MeasurementCharacteristic.SPECIFIC_GRAVITY);
    }
    if (profile.titratableAcidity() != null) {
      present.add(MeasurementCharacteristic.TITRATABLE_ACIDITY);
    }
    if (profile.tannin() != null) {
      present.add(MeasurementCharacteristic.TANNIN);
    }
    return present;
  }
}
