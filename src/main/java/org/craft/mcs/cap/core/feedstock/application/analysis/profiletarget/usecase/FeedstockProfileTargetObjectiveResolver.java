package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Resolves feedstock-specific target selections into the generic profile-target objective model.
 *
 * <p>The feedstock application layer accepts user-facing target selections: predefined
 * sugar-acidity zones or custom rectangles. This adapter keeps that translation in one place so
 * distance scoring and analysis candidate enrichment use exactly the same target semantics.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockProfileTargetObjectiveResolver {

  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @NonNull ProfileTargetObjective resolve(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock batch profile target command cannot be null");

    final SugarAcidityTarget target = switch (command.target()) {
      case PredefinedFeedstockProfileTargetSelection(SugarAcidityZone zone) ->
          targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleFeedstockProfileTargetSelection(RectangleSugarAcidityTargetGeometry geometry) ->
          new CustomRectangleSugarAcidityTarget(geometry);
    };

    final var tanninTarget = command.tanninTarget();
    if (tanninTarget == null) {
      return ProfileTargetObjective.of(target);
    }

    return ProfileTargetObjective.withTanninTarget(target, tanninTarget);
  }
}
