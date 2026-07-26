package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.traceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Adapts production creation events to batch traceability steps.
 *
 * <p>The adapter is the anti-corruption layer between production and traceability: production emits
 * a domain-specific "production batch was created" fact, while this adapter maps it to
 * {@code PRODUCTION_BATCH_CREATED}. The actual append joins the production transaction.
 */
@Component
@RequiredArgsConstructor
class BatchTraceabilityProductionBatchTraceRecorder implements ProductionBatchTraceRecorder {

  private final @NonNull AppendBatchTraceStepUseCase appendBatchTraceStepUseCase;
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

    final var productionSubject = new BatchTraceSubject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        productionBatch.batchId().value(),
        productionBatch.name().value()
    );
    final var sourceTransitions = sources.stream()
        .map(BatchTraceabilityProductionBatchTraceRecorder::sourceTransition)
        .toList();
    final var productionStateId = productionBatch.currentMaterialStateId();
    final var productionOutput = outputSubject(
        productionBatch,
        productionSubject,
        productionStateId
    );
    final var step = BatchTraceStep.create(
        productionBatch.workspaceId(),
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        sourceTransitions.stream()
            .map(SourceTransition::input)
            .toList(),
        outputs(sourceTransitions, productionOutput),
        requireNonNull(occurredAt, "trace occurred at cannot be null"),
        BatchTraceActorReference.accountUser(
            getCurrentAccountProfileUseCase.get().userId().value().toString()
        ),
        note
    );
    appendBatchTraceStepUseCase.append(step);
  }

  private static @NonNull SourceTransition sourceTransition(
      @NonNull ProductionBatchTraceSource source
  ) {
    requireNonNull(source, "production trace source cannot be null");
    final var displayName = source.displayName();
    final var subject = new BatchTraceSubject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        source.id(),
        displayName == null ? null : displayName.value()
    );
    final var remainingSourceStateId = source.remainingSourceStateId();
    final var input = new BatchTraceStepSubject(
        source.sourceStateId(),
        subject,
        new BatchTraceMaterialSnapshot(
            source.availableBefore(),
            source.profile(),
            source.confidence()
        )
    );
    final var output = new BatchTraceStepSubject(
        remainingSourceStateId,
        subject,
        new BatchTraceMaterialSnapshot(
            source.remaining(),
            source.profile(),
            source.confidence()
        )
    );
    return new SourceTransition(
        input,
        output
    );
  }

  private static @NonNull List<@NonNull BatchTraceStepSubject> outputs(
      @NonNull List<@NonNull SourceTransition> sourceTransitions,
      @NonNull BatchTraceStepSubject productionOutput
  ) {
    final var outputs = new ArrayList<BatchTraceStepSubject>(sourceTransitions.size() + 1);
    sourceTransitions.stream()
        .map(SourceTransition::output)
        .forEach(outputs::add);
    outputs.add(productionOutput);
    return List.copyOf(outputs);
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

  private record SourceTransition(
      @NonNull BatchTraceStepSubject input,
      @NonNull BatchTraceStepSubject output
  ) {
  }
}
