package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.PredictFeedstockBatchCurrentPotentialAbvUseCase;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.mapper.FeedstockBatchPotentialAbvRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class FeedstockBatchPotentialAbvController implements FeedstockBatchPotentialAbvApi {

  private final @NonNull PredictFeedstockBatchCurrentPotentialAbvUseCase predictPotentialAbvUseCase;

  @Override
  @GetMapping(value = "/feedstock-batches/{batchId}/abv-prediction", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull FeedstockBatchCurrentPotentialAbvResponse> predictPotentialAbv(
      @PathVariable @NonNull UUID batchId
  ) {
    final var prediction = predictPotentialAbvUseCase.predict(
        FeedstockBatchId.of(batchId)
    );

    return ResponseEntity.ok(
        FeedstockBatchPotentialAbvRestMapper.toResponse(prediction)
    );
  }
}
