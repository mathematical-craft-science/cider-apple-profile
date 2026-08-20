package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.readiness;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.FermentationStartReadinessCommand;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartReadinessView;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.AssessFermentationStartReadinessUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract.FeedstockFermentationStartReadinessView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out.FeedstockFermentationStartReadinessAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;


/** ACL from Feedstock fermentation readiness to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class FeedstockFermentationStartReadinessAdapter
    implements FeedstockFermentationStartReadinessAnalyzer {

  private final @NonNull AssessFermentationStartReadinessUseCase useCase;

  @Override
  public @NonNull FeedstockFermentationStartReadinessView assess(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<PhValue> ph,
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  ) {
    return toFeedstockView(useCase.assess(
        FermentationStartReadinessCommand.withoutPreparationActions(
            specificGravity,
            ph,
            yeastAssimilableNitrogen
        )
    ));
  }

  private static @NonNull FeedstockFermentationStartReadinessView toFeedstockView(
      @NonNull FermentationStartReadinessView result
  ) {
    return new FeedstockFermentationStartReadinessView(
        FeedstockFermentationStartReadinessView.Status.valueOf(result.status().name()),
        result.method(),
        result.checks().stream()
            .map(FeedstockFermentationStartReadinessAdapter::toFeedstockCheck)
            .toList()
    );
  }

  private static FeedstockFermentationStartReadinessView.@NonNull Check toFeedstockCheck(
      FermentationStartReadinessView.@NonNull Check check
  ) {
    final var managedByAction = check.managedByAction();
    return new FeedstockFermentationStartReadinessView.Check(
        FeedstockFermentationStartReadinessView.Factor.valueOf(check.factor().name()),
        FeedstockFermentationStartReadinessView.Severity.valueOf(check.severity().name()),
        FeedstockFermentationStartReadinessView.Code.valueOf(check.code().name()),
        check.message(),
        check.value(),
        check.unit(),
        managedByAction == null
            ? null
            : FeedstockFermentationStartReadinessView.PreparationAction.valueOf(managedByAction.name())
    );
  }
}
