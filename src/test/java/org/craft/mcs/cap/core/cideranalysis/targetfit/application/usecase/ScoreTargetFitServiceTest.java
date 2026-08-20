package org.craft.mcs.cap.core.cideranalysis.targetfit.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand.Candidate;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand.TargetObjective;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution.TargetAwareCompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;


class ScoreTargetFitServiceTest {

  private final ScoreTargetFitService service = new ScoreTargetFitService(
      new ZoneGeometrySugarAcidityTargetResolver(
          BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
      ),
      new BuiltInProfileAxisNormalizerSelector(),
      new BuiltInProfileTargetObjectiveFunctionFactory(),
      new TargetAwareCompositionContributionRoleAssessor()
  );

  @Test
  void ranksActualTargetMemberBeforeExcludedBoundaryWithSameClosureDistance() {
    final var result = service.score(command(
        new TargetFitCommand.TargetObjective(
            new TargetFitCommand.PredefinedTarget(JolicoeurSugarAcidityZoneCode.LOW_SUGAR_APPLES),
            null
        ),
        List.of(
            candidate("excluded-boundary", "1.045", "7.6", null),
            candidate("inside", "1.044", "7.6", null)
        )
    ));

    assertThat(result.scores())
        .extracting(score -> score.reference().id())
        .containsExactly("inside", "excluded-boundary");
    assertThat(result.scores())
        .extracting(TargetFitCandidateScoresView.CandidateScore::distance)
        .allSatisfy(distance -> assertThat(distance.value()).isEqualByComparingTo(BigDecimal.ZERO))
        .extracting(TargetFitCandidateScoresView.Distance::insideTarget)
        .containsExactly(true, false);
  }

  @Test
  void ranksUnscorableCandidateAfterScoredCandidate() {
    final var result = service.score(command(
        new TargetFitCommand.TargetObjective(
            new TargetFitCommand.PredefinedTarget(JolicoeurSugarAcidityZoneCode.PERFECT_APPLES),
            MeasuredValueRange.closed(TanninValue.of("1500"), TanninValue.of("2500"))
        ),
        List.of(
            candidate("missing-tannin", "1.070", "6.0", null),
            candidate("scored", "1.070", "6.0", "1800")
        )
    ));

    assertThat(result.scores())
        .extracting(score -> score.reference().id())
        .containsExactly("scored", "missing-tannin");
    assertThat(result.scores().getLast().distance())
        .returns(
            TargetFitCandidateScoresView.DistanceStatus.UNSCORABLE,
            TargetFitCandidateScoresView.Distance::status
        )
        .satisfies(distance -> assertThat(distance.missingAxes())
            .containsExactly(TargetFitCandidateScoresView.EvaluationAxis.TANNIN));
  }

  @Test
  void scoresCandidatesWithRequestedConfigurationAndSortsByDistance() {
    final var configuration = configuration(TargetFitScoringConfiguration.Normalizer.MIN_MAX);
    final var result = service.score(command(
        new TargetFitCommand.TargetObjective(
            new TargetFitCommand.PredefinedTarget(JolicoeurSugarAcidityZoneCode.PERFECT_APPLES),
            null
        ),
        configuration,
        List.of(
            candidate("far", "1.040", "9.0", null),
            candidate("inside", "1.070", "6.0", null),
            candidate("near", "1.055", "6.0", null)
        )
    ));

    assertThat(result)
        .returns(configuration, TargetFitCandidateScoresView::configuration)
        .satisfies(view -> assertThat(view.scores())
            .extracting(score -> score.reference().id())
            .containsExactly("inside", "near", "far"))
        .satisfies(view -> assertThat(view.scores().getFirst().distance().value())
            .isEqualByComparingTo(BigDecimal.ZERO));
  }

  @Test
  void requestedNormalizerChangesCalculatedDistance() {
    final var objective = new TargetFitCommand.TargetObjective(
        new TargetFitCommand.PredefinedTarget(JolicoeurSugarAcidityZoneCode.PERFECT_APPLES),
        null
    );
    final var candidates = List.of(candidate("candidate", "1.055", "6.0", null));

    final var minMaxDistance = requireNonNull(service.score(command(
        objective,
        configuration(TargetFitScoringConfiguration.Normalizer.MIN_MAX),
        candidates
    )).scores().getFirst().distance().value());
    final var sensitivityDistance = requireNonNull(service.score(command(
        objective,
        configuration(TargetFitScoringConfiguration.Normalizer.SENSITIVITY_UNIT),
        candidates
    )).scores().getFirst().distance().value());

    assertThat(sensitivityDistance).isGreaterThan(minMaxDistance);
  }

  private static @NonNull TargetFitCommand command(
      @NonNull TargetObjective objective,
      @NonNull List<@NonNull Candidate> candidates
  ) {
    return command(objective, configuration(TargetFitScoringConfiguration.Normalizer.MIN_MAX), candidates);
  }

  private static @NonNull TargetFitCommand command(
      @NonNull TargetObjective objective,
      @NonNull TargetFitScoringConfiguration configuration,
      @NonNull List<@NonNull Candidate> candidates
  ) {
    return new TargetFitCommand(
        objective,
        configuration,
        TargetFitCommand.IntervalPolicy.MIDPOINT,
        candidates
    );
  }

  private static @NonNull TargetFitScoringConfiguration configuration(
      TargetFitScoringConfiguration.@NonNull Normalizer normalizer
  ) {
    return new TargetFitScoringConfiguration(
        normalizer,
        TargetFitScoringConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        new TargetFitScoringConfiguration.ObjectiveWeights(
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE
        )
    );
  }

  private static @NonNull Candidate candidate(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin
  ) {
    return new TargetFitCommand.Candidate(
        new TargetFitCandidateReference("test", id),
        new TargetFitCommand.Characteristics(
            PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
            PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
            tannin == null ? null : PointCharacteristicValue.of(TanninValue.of(tannin))
        )
    );
  }
}
