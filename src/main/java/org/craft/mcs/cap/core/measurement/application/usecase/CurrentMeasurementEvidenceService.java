package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryCurrentMeasurementEvidenceUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectCurrentStateProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessPolicy;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Selects current measurement evidence by material-state lineage rather than global measurement time. */
@Component
@RequiredArgsConstructor
public final class CurrentMeasurementEvidenceService implements QueryCurrentMeasurementEvidenceUseCase {

  private final @NonNull CurrentMeasurementResultResolver resultResolver;
  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;
  private final @NonNull MeasurementSubjectCurrentStateProvider subjectCurrentStateProvider;
  private final @NonNull MeasurementFreshnessThresholdProvider freshnessThresholdProvider;
  private final @NonNull Clock clock;

  @Override
  public @NonNull CurrentMeasurementEvidenceView getCurrent(
      @NonNull MeasurementSubjectReference subject
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    final var workspaceId = subjectAccessControl.requireReadAccess(subject);
    return requireNonNull(
        queryInWorkspace(workspaceId, Set.of(subject)).get(subject),
        "current measurement evidence cannot be null"
    );
  }

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMeasurementEvidenceView>
      getAllCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSubjectReference> subjects
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(subjects, "measurement subjects cannot be null");
    if (subjects.isEmpty()) {
      return Map.of();
    }

    final var verifiedSubjects = subjects.stream()
        .map(subject -> requireNonNull(subject, "measurement subject cannot be null"))
        .collect(Collectors.toUnmodifiableSet());
    verifiedSubjects.stream()
        .map(MeasurementSubjectReference::type)
        .distinct()
        .forEach(subjectType -> subjectAccessControl.requireReadAccess(subjectType, workspaceId));
    return queryInWorkspace(workspaceId, verifiedSubjects);
  }

  private @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMeasurementEvidenceView>
      queryInWorkspace(
          @NonNull WorkspaceId workspaceId,
          @NonNull Set<@NonNull MeasurementSubjectReference> subjects
      ) {
    final var assessedAt = clock.instant();
    final var staleAfter = freshnessThresholdProvider.staleAfter(workspaceId);
    final var freshnessPolicy = new MeasurementFreshnessPolicy(staleAfter);
    final var currentStateIds = subjectCurrentStateProvider.currentStatesOf(workspaceId, subjects);
    final Map<MeasurementSubjectReference, List<CurrentMeasurementResultView>> resultsBySubject =
        new LinkedHashMap<>();
    resultResolver.resolve(workspaceId, currentStateIds).forEach(result -> resultsBySubject
        .computeIfAbsent(result.subject(), _ -> new ArrayList<>())
        .add(result));

    final Map<MeasurementSubjectReference, CurrentMeasurementEvidenceView> evidenceBySubject =
        new LinkedHashMap<>();
    subjects.forEach(subject -> {
      final var currentStateId = requireNonNull(
          currentStateIds.get(subject),
          "current material state id cannot be null"
      );
      final var results = resultsBySubject.getOrDefault(subject, List.of());
      evidenceBySubject.put(
        subject,
        new CurrentMeasurementEvidenceView(
            subject,
            currentStateId,
            results.stream()
                .map(result -> toCharacteristic(result, freshnessPolicy, assessedAt))
                .toList()
        )
      );
    });
    return Map.copyOf(evidenceBySubject);
  }

  private static @NonNull CurrentMeasurementCharacteristicView toCharacteristic(
      @NonNull CurrentMeasurementResultView result,
      @NonNull MeasurementFreshnessPolicy freshnessPolicy,
      @NonNull Instant assessedAt
  ) {
    final var origin = result.currentMaterialStateId().equals(result.evidenceMaterialStateId())
        ? MeasurementValueOrigin.MEASURED
        : MeasurementValueOrigin.INHERITED;
    return new CurrentMeasurementCharacteristicView(
        result.characteristic(),
        result.value(),
        origin,
        result.evidenceMaterialStateId(),
        result.measurementSetId(),
        result.recordId(),
        result.measuredAt(),
        result.provenance(),
        freshnessPolicy.status(result.measuredAt(), assessedAt)
    );
  }
}
