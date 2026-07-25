package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.InitializeMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Adapts production creation events to batch traceability steps.
 *
 * <p>The adapter is the anti-corruption layer between production and traceability: production emits
 * a domain-specific "production batch was created" fact, while this adapter maps it to
 * {@code PRODUCTION_BATCH_CREATED}. The actual append remains guarded by {@code MANDATORY}
 * transaction propagation in {@link InitializeMaterialStateUseCase}.
 */
@Component
@RequiredArgsConstructor
class BatchTraceabilityProductionBatchTraceRecorder implements ProductionBatchTraceRecorder {

  private final @NonNull InitializeMaterialStateUseCase initializeMaterialStateUseCase;
  private final @NonNull GetCurrentMaterialStateUseCase getCurrentMaterialStateUseCase;
  private final @NonNull GetCurrentAccountProfileUseCase getCurrentAccountProfileUseCase;

  @Override
  public void recordCreated(
      @NonNull ProductionBatch productionBatch,
      @NonNull List<@NonNull ProductionBatchTraceSource> sources,
      @NonNull Instant occurredAt,
      @Nullable String note
  ) {
    requireNonNull(productionBatch, "production batch cannot be null");
    sources = List.copyOf(requireNonNull(sources, "production trace sources cannot be null"));

    final var subject = new BatchTraceSubject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        productionBatch.batchId().value(),
        productionBatch.name().value()
    );
    final var stateId = MaterialStateId.newId();
    final var step = BatchTraceStep.create(
        productionBatch.workspaceId(),
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        inputSubjects(sources),
        List.of(outputSubject(productionBatch, subject, stateId)),
        requireNonNull(occurredAt, "trace occurred at cannot be null"),
        BatchTraceActorReference.accountUser(
            getCurrentAccountProfileUseCase.get().userId().value().toString()
        ),
        note
    );
    initializeMaterialStateUseCase.initialize(step, subject, stateId);
  }

  private @NonNull List<@NonNull BatchTraceStepSubject> inputSubjects(
      @NonNull List<@NonNull ProductionBatchTraceSource> sources
  ) {
    return sources.stream()
        .map(this::requireSourceState)
        .toList();
  }

  private @NonNull BatchTraceStepSubject requireSourceState(
      @NonNull ProductionBatchTraceSource source
  ) {
    final var persistedState = getCurrentMaterialStateUseCase.getState(source.materialStateId()).state();
    if (persistedState.subject().type() != BatchTraceSubjectType.FEEDSTOCK_BATCH
        || !persistedState.subject().id().equals(source.id())) {
      throw new ApplicationValidationException(
          "production source material state does not belong to the declared source"
      );
    }
    final var displayName = source.displayName() == null
        ? persistedState.subject().displayNameSnapshot()
        : source.displayName().value(); // NOSONAR
    return new BatchTraceStepSubject(
        source.materialStateId(),
        new BatchTraceSubject(
            persistedState.subject().type(),
            persistedState.subject().id(),
            displayName
        ),
        new BatchTraceMaterialSnapshot(
            source.volume(),
            source.profile(),
            source.confidence()
        )
    );
  }

  private static @NonNull BatchTraceStepSubject outputSubject(
      @NonNull ProductionBatch productionBatch,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId materialStateId
  ) {
    return new BatchTraceStepSubject(
        materialStateId,
        subject,
        new BatchTraceMaterialSnapshot(
            productionBatch.currentVolume(),
            toAxes(productionBatch.initialProfileSnapshot()),
            null
        )
    );
  }

  private static @NonNull ChemicalProfileAxes toAxes(@NonNull ProductionProfileSnapshot profile) {
    return ChemicalProfileAxes.of(
        new AxisValue(
            ChemicalAxis.SUGAR,
            profile.specificGravity().asBigDecimal(),
            AxisValueKind.CALCULATED,
            null,
            null
        ),
        new AxisValue(
            ChemicalAxis.ACIDITY,
            profile.titratableAcidity().asBigDecimal(),
            AxisValueKind.CALCULATED,
            null,
            null
        ),
        profile.tannin() == null
            ? null
            : new AxisValue(
                ChemicalAxis.TANNIN,
                profile.tannin().asBigDecimal(),
                AxisValueKind.CALCULATED,
                null,
                null
            ),
        profile.yeastAssimilableNitrogen() == null
            ? null
            : new AxisValue(
                ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
                profile.yeastAssimilableNitrogen().asBigDecimal(),
                AxisValueKind.CALCULATED,
                null,
                null
            )
    );
  }
}
