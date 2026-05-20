package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper.ProductionBatchRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(path = "/production-batches")
@RequiredArgsConstructor
public final class ProductionBatchController implements ProductionBatchApi {

  private final @NonNull CreateProductionBatchUseCase createProductionBatchUseCase;

  @Override
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public @NonNull ProductionBatchResponse create(@RequestBody @NonNull CreateProductionBatchRequest request) {
    final var command = ProductionBatchRestMapper.toCommand(request);
    return ProductionBatchRestMapper.toResponse(createProductionBatchUseCase.create(command));
  }
}
