package org.craft.mcs.cap.core.production.candidate.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.candidate.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Assesses current source evidence and saves a reusable production candidate. */
@Component
@RequiredArgsConstructor
public final class CreateProductionBatchCandidateService implements CreateProductionBatchCandidateUseCase {

  private final @NonNull ProductionSourceInspector sourceInspector;
  private final @NonNull ProductionBatchCandidateCalculator calculator;
  private final @NonNull ProductionBatchCandidateStore candidateStore;
  private final @NonNull ProductionAccessControl productionAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull ProductionBatchCandidateView create(
      @NonNull CreateProductionBatchCandidateCommand command
  ) {
    requireNonNull(command, "create production batch candidate command cannot be null");
    productionAccessControl.requireWriteAccess(command.workspaceId());

    return transactionHelper.callInTransactionNonNull(() -> createInTransaction(command));
  }

  private @NonNull ProductionBatchCandidateView createInTransaction(
      @NonNull CreateProductionBatchCandidateCommand command
  ) {
    final var sources = inspectSources(command);
    final var calculation = calculator.calculate(sources, command.calculationRequest());
    final var candidate = ProductionBatchCandidate.create(
        ProductionBatchCandidateId.newId(),
        command.workspaceId(),
        command.sourceComposition(),
        evidenceBySource(sources),
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

  private static @NonNull Map<@NonNull ProductionSourceReference, @NonNull ProductionSourceEvidenceReference>
      evidenceBySource(@NonNull List<@NonNull AvailableProductionSource> sources) {
    return sources.stream().collect(Collectors.toUnmodifiableMap(
        AvailableProductionSource::reference,
        AvailableProductionSource::evidenceReference
    ));
  }
}
