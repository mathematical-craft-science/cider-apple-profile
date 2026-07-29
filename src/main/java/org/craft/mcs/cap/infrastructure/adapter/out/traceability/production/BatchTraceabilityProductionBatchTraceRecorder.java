package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceCharacteristicSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialCharacteristics;
import org.craft.mcs.cap.core.traceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.model.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceType;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
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
      @NonNull List<@NonNull ConsumedProductionSource> sources,
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
      @NonNull ConsumedProductionSource source
  ) {
    requireNonNull(source, "production trace source cannot be null");
    final var displayName = source.displayName();
    final var subject = new BatchTraceSubject(
        toTraceSubjectType(source.reference().type()),
        source.reference().id(),
        displayName == null ? null : displayName.value()
    );
    final var remainingSourceStateId = source.remainingSourceStateId();
    final var input = new BatchTraceStepSubject(
        source.sourceStateId(),
        subject,
        new BatchTraceMaterialSnapshot(
            source.availableBefore(),
            toTraceCharacteristics(source.characteristics()),
            source.confidence()
        )
    );
    final var output = new BatchTraceStepSubject(
        remainingSourceStateId,
        subject,
        new BatchTraceMaterialSnapshot(
            source.remaining(),
            toTraceCharacteristics(source.characteristics()),
            source.confidence()
        )
    );
    return new SourceTransition(
        input,
        output
    );
  }

  private static @NonNull BatchTraceSubjectType toTraceSubjectType(
      @NonNull ProductionSourceType sourceType
  ) {
    return switch (requireNonNull(sourceType, "production source type cannot be null")) {
      case FEEDSTOCK_BATCH -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
    };
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
            toTraceCharacteristics(productionBatch.initialProfileSnapshot()),
            null
        )
    );
  }

  private static @NonNull BatchTraceMaterialCharacteristics toTraceCharacteristics(
      @NonNull ProductionProfileSnapshot profile
  ) {
    return new BatchTraceMaterialCharacteristics(
        BatchTraceCharacteristicSnapshot.calculated(profile.specificGravity()),
        BatchTraceCharacteristicSnapshot.calculated(profile.titratableAcidity()),
        profile.tannin() == null
            ? null
            : BatchTraceCharacteristicSnapshot.calculated(profile.tannin()),
        profile.yeastAssimilableNitrogen() == null
            ? null
            : BatchTraceCharacteristicSnapshot.calculated(profile.yeastAssimilableNitrogen())
    );
  }

  private static @NonNull BatchTraceMaterialCharacteristics toTraceCharacteristics(
      @NonNull ProductionSourceCharacteristics characteristics
  ) {
    return new BatchTraceMaterialCharacteristics(
        BatchTraceCharacteristicSnapshot.direct(characteristics.specificGravity()),
        BatchTraceCharacteristicSnapshot.direct(characteristics.titratableAcidity()),
        characteristics.tannin() == null
            ? null
            : BatchTraceCharacteristicSnapshot.direct(characteristics.tannin()),
        characteristics.yeastAssimilableNitrogen() == null
            ? null
            : BatchTraceCharacteristicSnapshot.direct(characteristics.yeastAssimilableNitrogen())
    );
  }

  private record SourceTransition(
      @NonNull BatchTraceStepSubject input,
      @NonNull BatchTraceStepSubject output
  ) {
  }
}
