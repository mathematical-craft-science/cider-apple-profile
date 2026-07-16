package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.in.GetProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchCandidateRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper.ProductionBatchCandidateRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** Exposes production-candidate assessment and retrieval through HTTP. */
@RestController
@RequestMapping(path = "/production-batch-candidates")
@RequiredArgsConstructor
public final class ProductionBatchCandidateController implements ProductionBatchCandidateApi {

  private final @NonNull CreateProductionBatchCandidateUseCase createCandidateUseCase;
  private final @NonNull GetProductionBatchCandidateUseCase getCandidateUseCase;
  private final @NonNull ProductionBatchCandidateRestMapper mapper;

  @Override
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public @NonNull ProductionBatchCandidateResponse create(
      @RequestBody @NonNull CreateProductionBatchCandidateRequest request
  ) {
    return ProductionBatchCandidateRestMapper.toResponse(createCandidateUseCase.create(mapper.toCommand(request)));
  }

  @Override
  @GetMapping(value = "/{candidateId}", produces = APPLICATION_JSON_VALUE)
  public @NonNull ProductionBatchCandidateResponse get(@PathVariable @NonNull UUID candidateId) {
    return ProductionBatchCandidateRestMapper.toResponse(
        getCandidateUseCase.get(ProductionBatchCandidateId.of(candidateId))
    );
  }
}
