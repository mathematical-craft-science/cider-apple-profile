package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScore;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScores;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;


class ScoreProfileTargetDistanceCandidatesServiceTest {

  private final ScoreProfileTargetDistanceCandidatesService service =
      new ScoreProfileTargetDistanceCandidatesService(
          new BuiltInProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds()),
          new BuiltInProfileTargetScoringPurposeNormalizerPolicy(),
          new BuiltInProfileTargetObjectiveFunctionFactory()
      );

  @Test
  void scoresCandidatesWithPurposeSelectedNormalizerAndSortsByDistance() {
    final var command = ScoreProfileTargetDistanceCandidatesCommand.withEqualWeights(
        ProfileTargetScoringPurpose.GRAPH_EXPLORATION,
        ProfileTargetObjective.of(target()),
        List.of(
            candidate("far", point("1.040", "9.0", null)),
            candidate("inside", point("1.070", "6.0", null)),
            candidate("near", point("1.055", "6.0", null))
        )
    );

    final var result = service.score(command);

    assertThat(result)
        .returns(ProfileTargetScoringPurpose.GRAPH_EXPLORATION, ProfileTargetDistanceCandidateScores::purpose)
        .returns(ProfileAxisNormalizerCode.MIN_MAX, ProfileTargetDistanceCandidateScores::normalizerCode)
        .satisfies(scored -> assertThat(scored.scores())
            .extracting(ScoreProfileTargetDistanceCandidatesServiceTest::candidateId)
            .containsExactly("inside", "near", "far"))
        .satisfies(scored -> assertThat(scored.scores().getFirst().distance().value())
            .isEqualByComparingTo("0"));
  }

  @Test
  void placesUnscorableCandidatesAfterScoredCandidates() {
    final var command = ScoreProfileTargetDistanceCandidatesCommand.withEqualWeights(
        ProfileTargetScoringPurpose.GRAPH_EXPLORATION,
        ProfileTargetObjective.withTanninTarget(
            target(),
            new TanninTargetRange(new BigDecimal("1500"), new BigDecimal("2500"))
        ),
        List.of(
            candidate("missing-tannin", point("1.070", "6.0", null)),
            candidate("with-tannin", point("1.070", "6.0", "1800"))
        )
    );

    final var result = service.score(command);

    assertThat(result)
        .satisfies(scored -> assertThat(scored.scores())
            .extracting(ScoreProfileTargetDistanceCandidatesServiceTest::candidateId)
            .containsExactly("with-tannin", "missing-tannin"))
        .satisfies(scored -> assertThat(scored.scores().get(1).distance().status())
            .isEqualTo(ProfileTargetDistanceStatus.UNSCORABLE))
        .satisfies(scored -> assertThat(scored.scores().get(1).distance().missingMeasurements())
            .containsExactly(ProfileEvaluationAxis.TANNIN));
  }

  @Test
  void selectedNormalizerChangesCalculatedDistance() {
    final var minMaxCommand = ScoreProfileTargetDistanceCandidatesCommand.withEqualWeights(
        ProfileTargetScoringPurpose.GRAPH_EXPLORATION,
        ProfileTargetObjective.of(target()),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );
    final var sensitivityCommand = ScoreProfileTargetDistanceCandidatesCommand.withEqualWeights(
        ProfileTargetScoringPurpose.EXPERT_CALIBRATION,
        ProfileTargetObjective.of(target()),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );

    final var minMaxDistance = requireNonNull(service.score(minMaxCommand).scores().getFirst().distance().value());
    final var sensitivityDistance = requireNonNull(
        service.score(sensitivityCommand).scores().getFirst().distance().value()
    );

    assertThat(sensitivityDistance).isGreaterThan(minMaxDistance);
  }

  @Test
  void blendingPlanningPurposeUsesEffectiveRangeNormalizer() {
    final var command = ScoreProfileTargetDistanceCandidatesCommand.withEqualWeights(
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        ProfileTargetObjective.of(target()),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );

    final var result = service.score(command);

    assertThat(result)
        .returns(ProfileTargetScoringPurpose.BLENDING_PLANNING, ProfileTargetDistanceCandidateScores::purpose)
        .returns(ProfileAxisNormalizerCode.EFFECTIVE_RANGE, ProfileTargetDistanceCandidateScores::normalizerCode);
  }

  @NonNull
  private static CustomRectangleSugarAcidityTarget target() {
    return new CustomRectangleSugarAcidityTarget(new RectangleSugarAcidityTargetGeometry(
        new BigDecimal("1.060"),
        new BigDecimal("1.080"),
        new BigDecimal("4.5"),
        new BigDecimal("7.5")
    ));
  }

  @NonNull
  private static ProfileTargetDistanceCandidate candidate(
      @NonNull String id,
      @NonNull SugarAcidityProfilePoint point
  ) {
    return new ProfileTargetDistanceCandidate(
        new ProfileTargetDistanceCandidateReference("test", id),
        point
    );
  }

  @NonNull
  private static String candidateId(@NonNull ProfileTargetDistanceCandidateScore score) {
    return score.reference().id();
  }

  @NonNull
  private static SugarAcidityProfilePoint point(
      @NonNull String sugar,
      @NonNull String acidity,
      @Nullable String tannin
  ) {
    return new SugarAcidityProfilePoint(
        axis(sugar),
        axis(acidity),
        tannin == null ? null : axis(tannin)
    );
  }

  @NonNull
  private static SugarAcidityAxisPoint axis(@NonNull String value) {
    return new SugarAcidityAxisPoint(
        new BigDecimal(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }
}
