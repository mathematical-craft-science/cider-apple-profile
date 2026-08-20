package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.mapper;

import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.contract.FeedstockNorthAmericanTanninLevel;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileFreshnessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileGraphResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfilePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileSummaryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockDataQualityIndicatorResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockDataQualityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockPlanningReadinessCheckResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockPlanningReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphRestMapper
    .missingCharacteristics;


/** Maps reusable Feedstock current-profile application bricks to their shared REST representations. */
public final class FeedstockCurrentProfileRestMapper {

  private FeedstockCurrentProfileRestMapper() {
  }

  public static @Nullable FeedstockCurrentProfileSummaryResponse toProfileSummary(
      @Nullable FeedstockCurrentProfileSummaryView view
  ) {
    if (view == null) {
      return null;
    }
    return FeedstockProfileSummaryRestMapper.toResponse(view);
  }

  public static @Nullable FeedstockCurrentProfileGraphResponse toGraph(
      @Nullable FeedstockSugarAcidityGraphView graphEvaluation,
      @Nullable FeedstockNorthAmericanTanninLevel northAmericanTanninLevel,
      @Nullable FeedstockCurrentProfileSummaryView currentProfile
  ) {
    if (graphEvaluation == null || currentProfile == null) {
      return null;
    }

    final var specificGravity = currentProfile.specificGravity();
    final var titratableAcidity = currentProfile.titratableAcidity();
    return new FeedstockCurrentProfileGraphResponse(
        graphEvaluation.plottable(),
        graphEvaluation.plottable()
            ? JolicoeurSugarAcidityGraphRestMapper.toResponse(
                graphEvaluation,
                northAmericanTanninLevel == null // NOSONAR
                    ? null
                    : NorthAmericanTanninLevel.valueOf(northAmericanTanninLevel.name()),
                requireNonNull(specificGravity),
                requireNonNull(titratableAcidity)
            )
            : null,
        missingCharacteristics(graphEvaluation)
    );
  }

  public static @Nullable FeedstockCurrentProfilePotentialAbvResponse toPotentialAbv(
      @Nullable FeedstockCurrentProfilePotentialAbvView view
  ) {
    return view == null
        ? null
        : new FeedstockCurrentProfilePotentialAbvResponse(
            ProfileCharacteristicRestMapper.valueKind(view.originalGravity()),
            PotentialAbvPredictionRestMapper.toResponse(view.prediction())
        );
  }

  public static @Nullable FeedstockCurrentProfileFreshnessResponse toFreshness(
      @Nullable FeedstockCurrentProfileFreshnessView view
  ) {
    return view == null
        ? null
        : new FeedstockCurrentProfileFreshnessResponse(view.status(), view.stale(), view.reason());
  }

  public static @NonNull FeedstockPlanningReadinessResponse toPlanningReadiness(
      @NonNull FeedstockPlanningReadinessView view
  ) {
    requireNonNull(view, "feedstock planning readiness view cannot be null");
    return new FeedstockPlanningReadinessResponse(
        view.status(),
        view.checks().stream()
            .map(FeedstockCurrentProfileRestMapper::toPlanningReadinessCheck)
            .toList()
    );
  }

  public static @NonNull FeedstockDataQualityResponse toDataQuality(
      @NonNull FeedstockCurrentProfileDataQualityView view
  ) {
    requireNonNull(view, "feedstock data quality view cannot be null");
    return new FeedstockDataQualityResponse(
        view.status(),
        view.indicators().stream()
            .map(FeedstockCurrentProfileRestMapper::toDataQualityIndicator)
            .toList()
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheckResponse toPlanningReadinessCheck(
      @NonNull FeedstockPlanningReadinessCheck check
  ) {
    requireNonNull(check, "feedstock planning readiness check cannot be null");
    return new FeedstockPlanningReadinessCheckResponse(
        check.factor(),
        check.severity(),
        check.code(),
        check.message()
    );
  }

  private static @NonNull FeedstockDataQualityIndicatorResponse toDataQualityIndicator(
      @NonNull FeedstockCurrentProfileDataQualityIndicator indicator
  ) {
    requireNonNull(indicator, "feedstock data quality indicator cannot be null");
    return new FeedstockDataQualityIndicatorResponse(
        indicator.code(),
        indicator.severity(),
        indicator.message()
    );
  }
}
