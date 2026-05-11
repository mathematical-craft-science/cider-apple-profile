package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileDistanceCandidate;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileDistanceCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ScoreBlendProfileDistanceCandidatesCommand;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxis;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BuiltInBlendProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ScoreBlendProfileDistanceCandidatesServiceTest {

  private final ScoreBlendProfileDistanceCandidatesService service =
      new ScoreBlendProfileDistanceCandidatesService(
          new BuiltInBlendProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds()),
          new BuiltInBlendProfileScoringPurposeNormalizerPolicy()
      );

  @Test
  void scoresCandidatesWithPurposeSelectedNormalizerAndSortsByDistance() {
    final var command = ScoreBlendProfileDistanceCandidatesCommand.withEqualWeights(
        BlendProfileScoringPurpose.GRAPH_EXPLORATION,
        BlendProfileObjective.of(target()),
        List.of(
            candidate("far", point("1.040", "9.0", null)),
            candidate("inside", point("1.070", "6.0", null)),
            candidate("near", point("1.055", "6.0", null))
        )
    );

    final var result = service.score(command);

    assertThat(result.purpose()).isEqualTo(BlendProfileScoringPurpose.GRAPH_EXPLORATION);
    assertThat(result.normalizerCode()).isEqualTo(BlendProfileAxisNormalizerCode.MIN_MAX);
    assertThat(result.scores()).extracting(score -> score.reference().id())
        .containsExactly("inside", "near", "far");
    assertThat(result.scores().getFirst().distance().value()).isEqualByComparingTo("0");
  }

  @Test
  void placesUnscorableCandidatesAfterScoredCandidates() {
    final var command = ScoreBlendProfileDistanceCandidatesCommand.withEqualWeights(
        BlendProfileScoringPurpose.GRAPH_EXPLORATION,
        BlendProfileObjective.withTanninTarget(
            target(),
            new TanninTargetRange(new BigDecimal("1500"), new BigDecimal("2500"))
        ),
        List.of(
            candidate("missing-tannin", point("1.070", "6.0", null)),
            candidate("with-tannin", point("1.070", "6.0", "1800"))
        )
    );

    final var result = service.score(command);

    assertThat(result.scores()).extracting(score -> score.reference().id())
        .containsExactly("with-tannin", "missing-tannin");
    assertThat(result.scores().get(1).distance().status())
        .isEqualTo(BlendProfileTargetDistanceStatus.UNSCORABLE);
    assertThat(result.scores().get(1).distance().missingMeasurements())
        .containsExactly(BlendProfileAxis.TANNIN);
  }

  @Test
  void selectedNormalizerChangesCalculatedDistance() {
    final var minMaxCommand = ScoreBlendProfileDistanceCandidatesCommand.withEqualWeights(
        BlendProfileScoringPurpose.GRAPH_EXPLORATION,
        BlendProfileObjective.of(target()),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );
    final var sensitivityCommand = ScoreBlendProfileDistanceCandidatesCommand.withEqualWeights(
        BlendProfileScoringPurpose.EXPERT_CALIBRATION,
        BlendProfileObjective.of(target()),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );

    final var minMaxDistance = service.score(minMaxCommand).scores().getFirst().distance().value();
    final var sensitivityDistance = service.score(sensitivityCommand).scores().getFirst().distance().value();

    assertThat(minMaxDistance).isNotNull();
    assertThat(sensitivityDistance).isNotNull();
    assertThat(sensitivityDistance).isGreaterThan(minMaxDistance);
  }

  @Test
  void blendingPlanningPurposeUsesEffectiveRangeNormalizer() {
    final var command = ScoreBlendProfileDistanceCandidatesCommand.withEqualWeights(
        BlendProfileScoringPurpose.BLENDING_PLANNING,
        BlendProfileObjective.of(target()),
        List.of(candidate("candidate", point("1.055", "6.0", null)))
    );

    final var result = service.score(command);

    assertThat(result.purpose()).isEqualTo(BlendProfileScoringPurpose.BLENDING_PLANNING);
    assertThat(result.normalizerCode()).isEqualTo(BlendProfileAxisNormalizerCode.EFFECTIVE_RANGE);
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
  private static BlendProfileDistanceCandidate candidate(
      @NonNull String id,
      @NonNull SugarAcidityProfilePoint point
  ) {
    return new BlendProfileDistanceCandidate(
        new BlendProfileDistanceCandidateReference("test", id),
        point
    );
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
