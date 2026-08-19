package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.batch.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.in.UpdateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchDetailsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.UpdateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper.ProductionBatchRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  private final @NonNull GetProductionBatchUseCase getProductionBatchUseCase;
  private final @NonNull ListProductionBatchesUseCase listProductionBatchesUseCase;
  private final @NonNull UpdateProductionBatchUseCase updateProductionBatchUseCase;

  @Override
  @GetMapping(produces = APPLICATION_JSON_VALUE)
  public @NonNull ProductionBatchListResponse list(@RequestParam @NonNull UUID workspaceId) {
    return ProductionBatchRestMapper.toListResponse(
        listProductionBatchesUseCase.list(new ListProductionBatchesQuery(WorkspaceId.of(workspaceId)))
    );
  }

  @Override
  @GetMapping(value = "/{batchId}", produces = APPLICATION_JSON_VALUE)
  public @NonNull ProductionBatchDetailsResponse get(@PathVariable @NonNull UUID batchId) {
    return ProductionBatchRestMapper.toDetailsResponse(
        getProductionBatchUseCase.get(ProductionBatchId.of(batchId))
    );
  }

  @Override
  @PutMapping(
      value = "/{batchId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ProductionBatchDetailsResponse update(
      @PathVariable @NonNull UUID batchId,
      @RequestBody @NonNull UpdateProductionBatchRequest request
  ) {
    return ProductionBatchRestMapper.toDetailsResponse(
        updateProductionBatchUseCase.update(ProductionBatchRestMapper.toCommand(
            ProductionBatchId.of(batchId),
            request
        ))
    );
  }

  @Override
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public @NonNull ProductionBatchResponse create(
      @RequestBody @NonNull CreateProductionBatchRequest request
  ) {
    final var command = ProductionBatchRestMapper.toCommand(request);
    return ProductionBatchRestMapper.toResponse(createProductionBatchUseCase.create(command));
  }
}
