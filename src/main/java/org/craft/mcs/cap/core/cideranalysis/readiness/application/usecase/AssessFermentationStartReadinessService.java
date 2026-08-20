package org.craft.mcs.cap.core.cideranalysis.readiness.application.usecase;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartPreparationAction;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartReadinessView;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.contract.FermentationStartReadinessView.Check;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.AssessFermentationStartReadinessUseCase;
import org.craft.mcs.cap.core.cideranalysis.readiness.application.port.in.FermentationStartReadinessCommand;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartProfile;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessCheck;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Application boundary for readiness assessment immediately before fermentation starts. */
@Component
@RequiredArgsConstructor
public final class AssessFermentationStartReadinessService implements AssessFermentationStartReadinessUseCase {

  private final @NonNull FermentationStartReadinessAssessor assessor;

  @Override
  public @NonNull FermentationStartReadinessView assess(
      @NonNull FermentationStartReadinessCommand command
  ) {
    requireNonNull(command, "fermentation-start readiness command cannot be null");
    final var assessment = assessor.assess(
        new FermentationStartProfile(
            command.specificGravity(),
            command.ph(),
            command.yeastAssimilableNitrogen()
        ),
        command.preparationActions().stream()
            .map(action -> FermentationStartPreparationActionType.valueOf(action.name()))
            .collect(Collectors.toUnmodifiableSet())
    );
    return new FermentationStartReadinessView(
        FermentationStartReadinessView.Status.valueOf(assessment.status().name()),
        assessment.method(),
        assessment.checks().stream()
            .map(AssessFermentationStartReadinessService::toView)
            .toList()
    );
  }

  private static @NonNull Check toView(
      @NonNull FermentationStartReadinessCheck check
  ) {
    final var managedAction = check.managedByAction();
    return new Check(
        FermentationStartReadinessView.Factor.valueOf(check.factor().name()),
        FermentationStartReadinessView.Severity.valueOf(check.severity().name()),
        FermentationStartReadinessView.Code.valueOf(check.code().name()),
        check.message(),
        check.value(),
        check.unit(),
        managedAction == null
            ? null
            : FermentationStartPreparationAction.valueOf(managedAction.name())
    );
  }
}
