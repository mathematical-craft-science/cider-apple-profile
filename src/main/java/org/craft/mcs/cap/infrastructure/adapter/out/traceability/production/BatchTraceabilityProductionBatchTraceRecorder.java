package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchCompositionTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Adapts production creation events to batch traceability steps.
 *
 * <p>The adapter is the anti-corruption layer between production and traceability: production emits
 * a domain-specific "production batch was created" fact, while this adapter maps it to
 * {@code CREATE_PRODUCTION_BATCH}. The actual append remains guarded by {@code MANDATORY}
 * transaction propagation in {@link AppendBatchTraceStepUseCase}.
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

    appendBatchTraceStepUseCase.append(BatchTraceStep.create(
        productionBatch.workspaceId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        inputSubjects(sources),
        List.of(outputSubject(productionBatch)),
        requireNonNull(occurredAt, "trace occurred at cannot be null"),
        BatchTraceActorReference.accountUser(
            getCurrentAccountProfileUseCase.get().userId().value().toString()
        ),
        note
    ));
  }

  private static @NonNull List<@NonNull BatchTraceStepSubject> inputSubjects(
      @NonNull List<@NonNull ProductionBatchTraceSource> sources
  ) {
    return sources.stream()
        .map(source -> BatchTraceStepSubject.capture(
            new BatchTraceSubject(
                toTraceSubjectType(source),
                source.id(),
                displayNameOf(source)
            ),
            new BatchTraceMaterialSnapshot(
                source.volume(),
                source.profile(),
                source.confidence()
            )
        ))
        .toList();
  }

  private static @NonNull BatchTraceSubjectType toTraceSubjectType(
      @NonNull ProductionBatchTraceSource source
  ) {
    return switch (source) {
      case ProductionBatchFeedstockTraceSource ignored -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
      case ProductionBatchCompositionTraceSource ignored -> BatchTraceSubjectType.COMPOSITION_RESULT;
    };
  }

  private static @Nullable String displayNameOf(@NonNull ProductionBatchTraceSource source) {
    final var displayName = source.displayName();
    return displayName == null ? null : displayName.value();
  }

  private static @NonNull BatchTraceStepSubject outputSubject(@NonNull ProductionBatch productionBatch) {
    return BatchTraceStepSubject.capture(
        new BatchTraceSubject(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            productionBatch.batchId().value().toString(),
            productionBatch.name().value()
        ),
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
            profile.sugar().asBigDecimal(),
            MeasurementValueKind.CALCULATED,
            null,
            null,
            null,
            null
        ),
        new AxisValue(
            ChemicalAxis.ACIDITY,
            profile.acidity().asBigDecimal(),
            MeasurementValueKind.CALCULATED,
            null,
            null,
            null,
            null
        ),
        profile.tannin() == null
            ? null
            : new AxisValue(
                ChemicalAxis.TANNINITY,
                profile.tannin().asBigDecimal(),
                MeasurementValueKind.CALCULATED,
                null,
                null,
                null,
                null
            ),
        profile.yeastAssimilableNitrogen() == null
            ? null
            : new AxisValue(
                ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
                profile.yeastAssimilableNitrogen().asBigDecimal(),
                MeasurementValueKind.CALCULATED,
                null,
                null,
                null,
                null
            )
    );
  }
}
