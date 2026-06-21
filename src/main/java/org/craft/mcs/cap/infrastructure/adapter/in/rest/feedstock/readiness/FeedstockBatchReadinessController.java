package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.AssessFeedstockBatchCurrentFermentationStartReadinessUseCase;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FeedstockBatchCurrentFermentationStartReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.mapper.FeedstockBatchReadinessRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockBatchReadinessController implements FeedstockBatchReadinessApi {

  private final @NonNull AssessFeedstockBatchCurrentFermentationStartReadinessUseCase
      assessFermentationStartReadinessUseCase;

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/fermentation-start-readiness", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentFermentationStartReadinessResponse>
      assessFermentationStartReadiness(@PathVariable @NonNull UUID batchId) {
    final var assessment = assessFermentationStartReadinessUseCase.assess(
        FeedstockBatchId.of(batchId)
    );

    return ResponseEntity.ok(
        FeedstockBatchReadinessRestMapper.toResponse(assessment)
    );
  }
}
