package org.craft.mcs.cap.core.cideranalysis.readiness.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.PhPlanningGuidanceView;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.AssessPhPlanningGuidanceUseCase;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.planning.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;


/** Application boundary for non-blocking pH guidance during production planning. */
@Component
@RequiredArgsConstructor
public final class AssessPhPlanningGuidanceService implements AssessPhPlanningGuidanceUseCase {

  private final @NonNull PhPlanningGuidancePolicy policy;

  @Override
  public @NonNull PhPlanningGuidanceView assess(
      @Nullable CharacteristicValue<PhValue> ph
  ) {
    final var guidance = policy.assess(ph);
    return new PhPlanningGuidanceView(
        PhPlanningGuidanceView.Code.valueOf(guidance.code().name()),
        guidance.message(),
        guidance.value(),
        guidance.source().code()
    );
  }
}
