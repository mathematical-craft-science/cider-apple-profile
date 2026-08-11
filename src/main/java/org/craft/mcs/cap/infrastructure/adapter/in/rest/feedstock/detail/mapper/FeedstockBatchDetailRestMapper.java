package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.mapper;

import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchDetailView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper.FeedstockBatchRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.mapper.FeedstockCurrentProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchCurrentProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto.FeedstockBatchDetailResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper.FeedstockBatchReadinessRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Maps feedstock detail application views to REST responses.
 *
 * <p>The mapper keeps transport concerns at the adapter boundary and delegates shared measurement
 * and profile-summary mapping to existing REST mappers.
 */
public final class FeedstockBatchDetailRestMapper {

  private FeedstockBatchDetailRestMapper() {
  }

  public static @NonNull FeedstockBatchDetailResponse toResponse(@NonNull FeedstockBatchDetailView view) {
    requireNonNull(view, "feedstock batch detail view cannot be null");
    return new FeedstockBatchDetailResponse(
        FeedstockBatchRestMapper.toResponse(view.batch()),
        view.batch().batch().currentMaterialStateId().value(),
        toResponse(view.currentProfile())
    );
  }

  private static @NonNull FeedstockBatchCurrentProfileResponse toResponse(
      @NonNull FeedstockBatchCurrentProfileView view
  ) {
    requireNonNull(view, "feedstock batch current profile view cannot be null");
    final var currentProfileSummary = view.currentProfileSummary();
    final var assessment = view.currentProfileAssessment();
    final var fermentationReadiness = assessment.fermentationStartReadiness();
    return new FeedstockBatchCurrentProfileResponse(
        currentProfileSummary == null
            ? null
            : FeedstockCurrentProfileRestMapper.toProfileSummary(currentProfileSummary),
        assessment.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        FeedstockCurrentProfileRestMapper.toGraph(
            assessment.graphEvaluation(),
            assessment.northAmericanTanninLevel(),
            currentProfileSummary
        ),
        FeedstockCurrentProfileRestMapper.toPotentialAbv(assessment.potentialAbv()),
        FeedstockCurrentProfileRestMapper.toFreshness(assessment.freshness()),
        FeedstockCurrentProfileRestMapper.toPlanningReadiness(assessment.planningReadiness()),
        FeedstockCurrentProfileRestMapper.toDataQuality(assessment.dataQuality()),
        fermentationReadiness == null
            ? null
            : FeedstockBatchReadinessRestMapper.toResponse(fermentationReadiness)
    );
  }

}
