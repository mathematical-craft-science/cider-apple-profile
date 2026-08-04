package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closedOpen;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScore;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;


class ScoreProfileTargetDistanceCandidatesServiceTest {

  private final ScoreProfileTargetDistanceCandidatesService service =
      new ScoreProfileTargetDistanceCandidatesService(
          new BuiltInProfileAxisNormalizerSelector(),
          new BuiltInProfileTargetObjectiveFunctionFactory()
      );

  @Test
  void scoresCandidatesWithExplicitConfigurationAndSortsByDistance() {
    final var command = new ScoreProfileTargetDistanceCandidatesCommand(
        ProfileTargetObjective.of(target()),
        configuration(ProfileAxisNormalizerCode.MIN_MAX),
        List.of(
            candidate("far", point("1.040", "9.0", null)),
            candidate("inside", point("1.070", "6.0", null)),
            candidate("near", point("1.055", "6.0", null))
        )
    );

    final var result = service.score(command);

    assertThat(result)
        .returns(
            ProfileAxisNormalizerCode.MIN_MAX,
            scored -> scored.configuration().normalizerCode()
        )
        .satisfies(scored -> assertThat(scored.scores())
            .extracting(ScoreProfileTargetDistanceCandidatesServiceTest::candidateId)
            .containsExactly("inside", "near", "far"))
        .satisfies(scored -> assertThat(scored.scores().getFirst().distance().value())
            .isEqualByComparingTo("0"));
  }

  @Test
  void placesUnscorableCandidatesAfterScoredCandidates() {
    final var command = new ScoreProfileTargetDistanceCandidatesCommand(
        ProfileTargetObjective.withTanninTarget(
            target(),
            new TanninTargetRange(TanninValue.of("1500"), TanninValue.of("2500"))
        ),
        configuration(ProfileAxisNormalizerCode.MIN_MAX),
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
        .satisfies(scored -> assertThat(scored.scores().get(1).distance().missingAxes())
            .containsExactly(ProfileEvaluationAxis.TANNIN));
  }

  @Test
  void ranksTargetMemberBeforeExcludedBoundaryAtTheSameClosureDistance() {
    final var command = new ScoreProfileTargetDistanceCandidatesCommand(
        ProfileTargetObjective.of(halfOpenTarget()),
        configuration(ProfileAxisNormalizerCode.MIN_MAX),
        List.of(
            candidate("excluded-boundary", point("1.045", "7.6", null)),
            candidate("inside", point("1.044", "7.6", null))
        )
    );

    final var result = service.score(command);

    assertThat(result.scores())
        .extracting(ScoreProfileTargetDistanceCandidatesServiceTest::candidateId)
        .containsExactly("inside", "excluded-boundary");
    assertThat(result.scores())
        .extracting(ProfileTargetDistanceCandidateScore::distance)
        .allSatisfy(distance -> assertThat(distance.value()).isEqualByComparingTo(BigDecimal.ZERO))
        .extracting(ProfileTargetDistance::insideTarget)
        .containsExactly(true, false);
  }

  @Test
  void selectedNormalizerChangesCalculatedDistance() {
    final var minMaxCommand = new ScoreProfileTargetDistanceCandidatesCommand(
        ProfileTargetObjective.of(target()),
        configuration(ProfileAxisNormalizerCode.MIN_MAX),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );
    final var sensitivityCommand = new ScoreProfileTargetDistanceCandidatesCommand(
        ProfileTargetObjective.of(target()),
        configuration(ProfileAxisNormalizerCode.SENSITIVITY_UNIT),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );

    final var minMaxDistance = requireNonNull(service.score(minMaxCommand).scores().getFirst().distance().value());
    final var sensitivityDistance = requireNonNull(
        service.score(sensitivityCommand).scores().getFirst().distance().value()
    );

    assertThat(sensitivityDistance).isGreaterThan(minMaxDistance);
  }

  @Test
  void preservesResolvedConfigurationInResult() {
    final var command = new ScoreProfileTargetDistanceCandidatesCommand(
        ProfileTargetObjective.of(target()),
        configuration(ProfileAxisNormalizerCode.EFFECTIVE_RANGE),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );

    final var result = service.score(command);

    assertThat(result)
        .returns(
            ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
            scored -> scored.configuration().normalizerCode()
        );
  }

  private static @NonNull ProfileTargetScoringConfiguration configuration(
      @NonNull ProfileAxisNormalizerCode normalizerCode
  ) {
    return new ProfileTargetScoringConfiguration(
        normalizerCode,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileTargetObjectiveWeights.balanced()
    );
  }

  @NonNull
  private static CustomRectangleSugarAcidityTarget target() {
    return new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            closed(
                SpecificGravityValue.of("1.060"),
                SpecificGravityValue.of("1.080")
            ),
            closed(
                TitratableAcidityValue.of("4.5"),
                TitratableAcidityValue.of("7.5")
            )
        )
    );
  }

  private static @NonNull CustomRectangleSugarAcidityTarget halfOpenTarget() {
    return new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            closedOpen(
                SpecificGravityValue.of("1.020"),
                SpecificGravityValue.of("1.045")
            ),
            closedOpen(
                TitratableAcidityValue.of("4.5"),
                TitratableAcidityValue.of("10.0")
            )
        )
    );
  }

  @NonNull
  private static ProfileTargetDistanceCandidate candidate(
      @NonNull String id,
      @NonNull ProfileTargetScoringInput input
  ) {
    return new ProfileTargetDistanceCandidate(
        new ProfileTargetDistanceCandidateReference("test", id),
        input
    );
  }

  @NonNull
  private static String candidateId(@NonNull ProfileTargetDistanceCandidateScore score) {
    return score.reference().id();
  }

  @NonNull
  private static ProfileTargetScoringInput point(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin
  ) {
    return new ProfileTargetScoringInput(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
        tannin == null ? null : PointCharacteristicValue.of(TanninValue.of(tannin))
    );
  }
}
