package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.readiness;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.AssessPhPlanningGuidanceUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract.FeedstockPhPlanningGuidanceView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out.FeedstockPhPlanningGuidanceAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;


/** ACL from Feedstock pH planning guidance to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class FeedstockPhPlanningGuidanceAdapter implements FeedstockPhPlanningGuidanceAnalyzer {

  private final @NonNull AssessPhPlanningGuidanceUseCase useCase;

  @Override
  public @NonNull FeedstockPhPlanningGuidanceView assess(
      @Nullable CharacteristicValue<PhValue> ph
  ) {
    final var result = useCase.assess(ph);
    return new FeedstockPhPlanningGuidanceView(
        FeedstockPhPlanningGuidanceView.Code.valueOf(result.code().name()),
        result.message(),
        result.value()
    );
  }
}
