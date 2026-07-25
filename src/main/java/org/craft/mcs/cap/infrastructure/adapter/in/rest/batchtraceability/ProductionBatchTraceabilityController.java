package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.QueryBatchTraceabilityUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceabilityGraphResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.mapper.BatchTraceabilityRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Thin REST adapter for production-rooted batch traceability queries. */
@RestController
@RequiredArgsConstructor
public final class ProductionBatchTraceabilityController implements ProductionBatchTraceabilityApi {

  private final @NonNull QueryBatchTraceabilityUseCase queryBatchTraceabilityUseCase;

  @Override
  @GetMapping(value = "/production-batches/{batchId}/traceability", produces = APPLICATION_JSON_VALUE)
  public @NonNull BatchTraceabilityGraphResponse traceability(@PathVariable @NonNull UUID batchId) {
    return BatchTraceabilityRestMapper.toResponse(
        queryBatchTraceabilityUseCase.sourceGraphFor(
            BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, batchId)
        )
    );
  }
}
