package org.craft.mcs.cap.core.profileanalysis.testing;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.MidpointGraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedCompositionCharacteristics;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.profileanalysis.graph.application.service.JolicoeurSugarAcidityGraphEvaluator;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.GeometryJolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Creates consistently configured profile-analysis services for focused tests. */
public final class ProfileAnalysisTestFixtures {

  private ProfileAnalysisTestFixtures() {
  }

  public static @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter() {
    return chemicalProfileAssessmentAdapter(
        BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
    );
  }

  public static @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    final JolicoeurSugarAcidityZoneAssessor zoneAssessor =
        new GeometryJolicoeurSugarAcidityZoneAssessor(geometryProfile);
    return new ChemicalProfileAssessmentAdapter(
        new ChemicalProfileAxisExtractor(),
        new JolicoeurSugarAcidityGraphEvaluator(
            new MidpointGraphRepresentativeValuePolicy(),
            zoneAssessor
        ),
        new NorthAmericanProfileCalculator(
            BuiltInCiderClassificationThresholds.northAmerican()
        ),
        new ChemicalProfileCapabilityAssessor()
    );
  }

  public static @NonNull ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher() {
    return new ChemicalProfileAssessmentMatcher(new ChemicalProfileCapabilityAssessor());
  }

  public static @NonNull CompositionSourceCharacteristics compositionSourceCharacteristics(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin,
      @Nullable String yeastAssimilableNitrogen
  ) {
    return new CompositionSourceCharacteristics(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
        tannin == null ? null : PointCharacteristicValue.of(TanninValue.of(tannin)),
        yeastAssimilableNitrogen == null
            ? null
            : PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of(yeastAssimilableNitrogen))
    );
  }

  public static @NonNull CalculatedCompositionCharacteristics calculatedCompositionCharacteristics() {
    return new CalculatedCompositionCharacteristics(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0"),
        null,
        null
    );
  }
}
