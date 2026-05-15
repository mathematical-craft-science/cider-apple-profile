package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
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


@RestController
@RequestMapping(path = "/production-batches")
@RequiredArgsConstructor
public final class ProductionBatchController {

  private final @NonNull CreateProductionBatchUseCase createProductionBatchUseCase;
  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public @NonNull ProductionBatchResponse create(@RequestBody @NonNull CreateProductionBatchRequest request) {
    final var command = ProductionBatchRestMapper.toCommand(request, targetResolver);
    return ProductionBatchRestMapper.toResponse(createProductionBatchUseCase.create(command));
  }
}
