package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
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
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(query, "feedstock batch profile target query cannot be null");

    final SugarAcidityTarget target = switch (query.target()) {
      case PredefinedFeedstockProfileTargetSelection(JolicoeurSugarAcidityZone zone) ->
          targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleFeedstockProfileTargetSelection(
          RectangleSugarAcidityTargetGeometry geometry
      ) ->
          new CustomRectangleSugarAcidityTarget(geometry);
    };

    final var tanninTarget = query.tanninTarget();
    if (tanninTarget == null) {
      return ProfileTargetObjective.of(target);
    }

    return ProfileTargetObjective.withTanninTarget(target, tanninTarget);
  }
}
