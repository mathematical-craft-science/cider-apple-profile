package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheck;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.in.FeedstockBatchCurrentFermentationStartReadinessResult;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FeedstockBatchCurrentFermentationStartReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessCheckResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchReadinessRestMapper {

  private FeedstockBatchReadinessRestMapper() {
  }

  public static @NonNull FeedstockBatchCurrentFermentationStartReadinessResponse toResponse(
      @NonNull FeedstockBatchCurrentFermentationStartReadinessResult result
  ) {
    requireNonNull(result, "feedstock batch current fermentation-start readiness result cannot be null");

    return new FeedstockBatchCurrentFermentationStartReadinessResponse(
        result.batchId().value(),
        result.workspaceId().value(),
        result.currentMaterialStateId().value(),
        result.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        toResponse(result.assessment())
    );
  }

  public static @NonNull FermentationStartReadinessAssessmentResponse toResponse(
      @NonNull FermentationStartReadinessAssessment assessment
  ) {
    requireNonNull(assessment, "fermentation-start readiness assessment cannot be null");

    return new FermentationStartReadinessAssessmentResponse(
        assessment.status(),
        assessment.method(),
        assessment.checks().stream()
            .map(FeedstockBatchReadinessRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FermentationStartReadinessCheckResponse toResponse(
      @NonNull FermentationStartReadinessCheck check
  ) {
    requireNonNull(check, "fermentation-start readiness check cannot be null");

    return new FermentationStartReadinessCheckResponse(
        check.factor(),
        check.severity(),
        check.code(),
        check.message(),
        toQuantityResponse(check.value(), check.unit()),
        check.managedByAction() == null ? null : check.managedByAction().name() // NOSONAR
    );
  }

  private static @Nullable DecimalQuantityResponse toQuantityResponse(
      @Nullable BigDecimal value,
      @Nullable String unit
  ) {
    return value == null || unit == null ? null : new DecimalQuantityResponse(value, unit);
  }
}
