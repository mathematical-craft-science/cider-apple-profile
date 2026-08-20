package org.craft.mcs.cap.core.production.planning.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.planning.application.authorization.port.out.ProductionPlanningAccessControl;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.planning.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.planning.application.port.in.CreateProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceSnapshotValidator;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Assesses current source evidence and saves a reusable production candidate. */
@Component
@RequiredArgsConstructor
public final class CreateProductionBatchCandidateService implements CreateProductionBatchCandidateUseCase {

  private static final @NonNull Comparator<AvailableProductionSource> SOURCE_VALIDATION_ORDER =
      Comparator.comparing(
              AvailableProductionSource::reference,
              Comparator.comparing(ProductionSourceReference::type)
                  .thenComparing(ProductionSourceReference::id)
          );

  private final @NonNull ProductionSourceInspector sourceInspector;
  private final @NonNull ProductionSourceSnapshotValidator sourceSnapshotValidator;
  private final @NonNull ProductionBatchCandidateCalculator calculator;
  private final @NonNull ProductionBatchCandidateStore candidateStore;
  private final @NonNull ProductionPlanningAccessControl accessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull ProductionBatchCandidateView create(
      @NonNull CreateProductionBatchCandidateCommand command
  ) {
    requireNonNull(command, "create production batch candidate command cannot be null");
    accessControl.requireWriteAccess(command.workspaceId());

    final var sources = transactionHelper.callInTransactionNonNull(() -> inspectSources(command));
    final var calculation = calculator.calculate(sources, command.calculationRequest());
    return transactionHelper.callInTransactionNonNull(() -> persistValidated(command, sources, calculation));
  }

  private @NonNull ProductionBatchCandidateView persistValidated(
      @NonNull CreateProductionBatchCandidateCommand command,
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculation calculation
  ) {
    sources.stream()
        .sorted(SOURCE_VALIDATION_ORDER)
        .forEach(source -> sourceSnapshotValidator.validate(command.workspaceId(), source));
    final var candidate = ProductionBatchCandidate.create(
        ProductionBatchCandidateId.newId(),
        command.workspaceId(),
        command.sourceComposition(),
        stateEvidenceBySource(sources),
        calculation.profile()
    );
    final var saved = candidateStore.create(new ProductionBatchCandidateSnapshot(
        candidate,
        calculation.potentialAbv(),
        calculation.normalizerCode(),
        calculation.targetDistance(),
        calculation.warnings()
    ));
    return ProductionBatchCandidateViewMapper.toView(saved);
  }

  private @NonNull List<@NonNull AvailableProductionSource> inspectSources(
      @NonNull CreateProductionBatchCandidateCommand command
  ) {
    return command.sourceComposition().sources().stream()
        .map(source -> sourceInspector.inspect(command.workspaceId(), source))
        .toList();
  }

  private static @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionCandidateSourceEvidence>
      stateEvidenceBySource(@NonNull List<@NonNull AvailableProductionSource> sources) {
    return sources.stream().collect(Collectors.toUnmodifiableMap(
        AvailableProductionSource::reference,
        source -> new ProductionCandidateSourceEvidence(
            source.evidenceReference(),
            source.materialStateId()
        )
    ));
  }
}
