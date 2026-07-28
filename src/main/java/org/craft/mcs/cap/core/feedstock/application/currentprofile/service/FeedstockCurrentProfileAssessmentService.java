package org.craft.mcs.cap.core.feedstock.application.currentprofile.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.application.compatibility.ChemicalProfileFermentationStartAdapter;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Composes the reusable current-profile assessments shared by Feedstock application read models. */
@Component
@RequiredArgsConstructor
public final class FeedstockCurrentProfileAssessmentService {

  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;
  private final @NonNull FermentationStartReadinessAssessor fermentationStartReadinessAssessor;
  private final @NonNull FeedstockCurrentProfileFreshnessAssessor freshnessAssessor;
  private final @NonNull FeedstockPlanningReadinessAssessor planningReadinessAssessor;
  private final @NonNull FeedstockProfileDataQualityAssessor dataQualityAssessor;

  public @NonNull FeedstockCurrentProfileAssessmentView assess(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    if (currentProfile == null) {
      return new FeedstockCurrentProfileAssessmentView(
          List.of(),
          null,
          null,
          null,
          null,
          planningReadinessAssessor.assessWithoutCurrentProfile(batch),
          dataQualityAssessor.assessWithoutCurrentProfile(),
          null
      );
    }

    final var profileAssessment = chemicalProfileAssessmentAdapter.project(currentProfile.chemicalProfile());
    final var freshness = freshnessAssessor.assess(currentProfile);
    return new FeedstockCurrentProfileAssessmentView(
        currentProfile.evidence(),
        freshness,
        profileAssessment,
        potentialAbv(profileAssessment),
        yeastAssimilableNitrogen(profileAssessment),
        planningReadinessAssessor.assess(batch, currentProfile.chemicalProfile()),
        dataQualityAssessor.assess(
            currentProfile.chemicalProfile(),
            profileAssessment,
            freshness,
            currentProfile.evidence()
        ),
        fermentationStartReadinessAssessor.assess(
            ChemicalProfileFermentationStartAdapter.adapt(currentProfile.chemicalProfile())
        )
    );
  }

  private @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv(
      @NonNull ChemicalProfileAssessment assessment
  ) {
    final var capability = capabilityAssessor.assess(
        assessment.axes(),
        ChemicalProfileCapability.ABV_PREDICTION
    );
    if (!capability.capable()) {
      return null;
    }

    final var specificGravity = assessment.axes().requireValueOf(ChemicalAxis.SPECIFIC_GRAVITY);
    return new FeedstockCurrentProfilePotentialAbvView(
        specificGravity.valueKind(),
        potentialAbvPredictor.predict(SpecificGravityValue.of(specificGravity.value()))
    );
  }

  private static @Nullable AxisValue yeastAssimilableNitrogen(
      @NonNull ChemicalProfileAssessment assessment
  ) {
    return assessment.axes()
        .valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN)
        .orElse(null); // NOSONAR
  }
}
