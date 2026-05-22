package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.ScoreFeedstockBatchProfileTargetCommand;
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
      case PredefinedFeedstockProfileTargetSelection predefined -> targetResolver.resolvePredefinedZone(predefined.zone()); // NOSONAR
      case CustomRectangleFeedstockProfileTargetSelection custom -> new CustomRectangleSugarAcidityTarget(custom.geometry()); // NOSONAR
    };

    return command.tanninTarget() == null
        ? ProfileTargetObjective.of(target)
        : ProfileTargetObjective.withTanninTarget(target, command.tanninTarget()); // NOSONAR
  }
}
