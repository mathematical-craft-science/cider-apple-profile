package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchCandidateRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCandidateSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionPlanningRestDefaults;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


/** Maps production-candidate transport data while resolving target specifications at the REST boundary. */
@Component
@RequiredArgsConstructor
public final class ProductionBatchCandidateRestMapper {

  private final @NonNull SugarAcidityTargetResolver targetResolver;

  public @NonNull CreateProductionBatchCandidateCommand toCommand(
      @NonNull CreateProductionBatchCandidateRequest request
  ) {
    requireNonNull(request, "create production batch candidate request cannot be null");
    final var sources = requireNonNull(request.sources(), "production candidate sources cannot be null")
        .stream()
        .map(ProductionBatchCandidateRestMapper::toSourceComponent)
        .toList();
    final var objectiveFunction = requireNonNull(
        requireNonNullElse(
            request.objectiveFunction(),
            CompositionPlanningRestDefaults.profileTargetObjectiveFunction()
        ),
        "production candidate objective function cannot be null"
    );
    final var tanninPolicy = requireNonNull(
        requireNonNullElse(
            request.tanninPolicy(),
            CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        ),
        "production candidate tannin policy cannot be null"
    );
    final var normalizer = requireNonNull(
        requireNonNullElse(
            request.normalizer(),
            CompositionPlanningRestDefaults.profileTargetNormalizer()
        ),
        "production candidate profile target normalizer cannot be null"
    );
    final var calculator = requireNonNull(
        requireNonNullElse(
            request.calculator(),
            CompositionCalculatorCode.MASS_BALANCE
        ),
        "production candidate calculator cannot be null"
    );

    return new CreateProductionBatchCandidateCommand(
        WorkspaceId.of(requireNonNull(request.workspaceId(), "workspace id cannot be null")),
        ProductionSourceComposition.of(sources),
        new ProductionBatchCandidateCalculationRequest(
            tanninPolicy,
            ProfileTargetRestMapper.toTargetObjective(
                request.target(),
                request.tanninTarget(),
                targetResolver
            ),
            new ProfileTargetScoringConfiguration(
                normalizer,
                objectiveFunction,
                ProfileTargetRestMapper.toWeights(request.weights())
            ),
            calculator
        )
    );
  }

  public static @NonNull ProductionBatchCandidateResponse toResponse(
      @NonNull ProductionBatchCandidateView candidate
  ) {
    requireNonNull(candidate, "production batch candidate cannot be null");
    final var confirmedProductionBatchId = candidate.confirmedProductionBatchId();
    return new ProductionBatchCandidateResponse(
        candidate.candidateId().value(),
        candidate.workspaceId().value(),
        candidate.status(),
        ProductionBatchCompositionRestMapper.toResponse(candidate),
        confirmedProductionBatchId == null
            ? null
            : confirmedProductionBatchId.value()
    );
  }

  private static @NonNull ProductionSourceComponent toSourceComponent(
      @NonNull ProductionBatchCandidateSourceRequest request
  ) {
    requireNonNull(request, "create production batch source request cannot be null");
    final UUID feedstockBatchId = requireNonNull(
        request.feedstockBatchId(),
        "feedstock batch id cannot be null"
    );
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(feedstockBatchId),
        VolumeValue.ofLiters(requireNonNull(request.volumeLiters(), "production source volume cannot be null"))
    );
  }
}
