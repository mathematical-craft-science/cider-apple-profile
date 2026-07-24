package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.projection.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds a composite current profile without allowing a partial new set to hide older axes.
 */
@Component
@RequiredArgsConstructor
public final class CurrentMeasurementProfileProjector
    implements ProjectCurrentMeasurementProfileUseCase {

  private final @NonNull CurrentMeasurementResultQuery resultQuery;
  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;
  private final @NonNull MeasurementFreshnessThresholdProvider freshnessThresholdProvider;
  private final @NonNull Clock clock;

  @Override
  public @NonNull Optional<@NonNull CurrentMeasurementProfileView> project(
      @NonNull MeasurementSubjectReference subject
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    final var workspaceId = subjectAccessControl.requireReadAccess(subject);
    return Optional.ofNullable(projectInWorkspace(workspaceId, Set.of(subject)).get(subject));
  }

  @Override
  public @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMeasurementProfileView> projectAll(
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
    return projectInWorkspace(workspaceId, verifiedSubjects);
  }

  private @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull CurrentMeasurementProfileView>
      projectInWorkspace(
          @NonNull WorkspaceId workspaceId,
          @NonNull Set<@NonNull MeasurementSubjectReference> subjects
      ) {
    final var staleAfter = freshnessThresholdProvider.staleAfter(workspaceId);
    final Map<MeasurementSubjectReference, EnumMap<MeasurementCharacteristic, CurrentMeasurementResultView>>
        selectedBySubject = new HashMap<>();
    resultQuery.findBySubjects(workspaceId, subjects)
        .forEach(result -> selectedBySubject
            .computeIfAbsent(
                result.subject(),
                _ -> new EnumMap<>(MeasurementCharacteristic.class)
            )
            .merge(
                result.characteristic(),
                result,
                CurrentMeasurementProfileProjector::latest
            ));

    final Map<MeasurementSubjectReference, CurrentMeasurementProfileView> profiles = new LinkedHashMap<>();
    subjects.forEach(subject -> {
      final var selected = selectedBySubject.get(subject);
      if (selected != null && containsChemicalCharacteristic(selected)) {
        profiles.put(subject, toProfileView(selected, staleAfter, clock.instant()));
      }
    });
    return Map.copyOf(profiles);
  }

  private static @NonNull CurrentMeasurementResultView latest(
      @NonNull CurrentMeasurementResultView first,
      @NonNull CurrentMeasurementResultView second
  ) {
    final var comparator = Comparator
        .comparing(CurrentMeasurementResultView::measuredAt)
        .thenComparing(selected -> selected.recordId().value());
    return comparator.compare(first, second) >= 0 ? first : second;
  }

  private static @NonNull CurrentMeasurementProfileView toProfileView(
      @NonNull EnumMap<MeasurementCharacteristic, CurrentMeasurementResultView> selected,
      @NonNull Duration staleAfter,
      @NonNull Instant assessedAt
  ) {
    final var profile = MeasurementChemicalProfileProjector.project(selected.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().value()
            )))
        .orElseThrow();
    final var evidence = selected.entrySet().stream()
        .filter(entry -> MeasurementChemicalProfileProjector.isChemical(entry.getKey()))
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> new MeasurementEvidenceView(
            entry.getKey(),
            entry.getValue().measurementSetId(),
            entry.getValue().recordId(),
            entry.getValue().measuredAt(),
            entry.getValue().provenance(),
            freshnessStatus(entry.getValue().measuredAt(), staleAfter, assessedAt)
        ))
        .toList();
    return new CurrentMeasurementProfileView(profile, evidence);
  }

  private static @NonNull MeasurementFreshnessStatus freshnessStatus(
      @NonNull Instant measuredAt,
      @NonNull Duration staleAfter,
      @NonNull Instant assessedAt
  ) {
    return assessedAt.isBefore(measuredAt.plus(staleAfter))
        ? MeasurementFreshnessStatus.CURRENT
        : MeasurementFreshnessStatus.STALE;
  }

  private static boolean containsChemicalCharacteristic(
      @NonNull EnumMap<MeasurementCharacteristic, CurrentMeasurementResultView> selected
  ) {
    return selected.keySet().stream().anyMatch(MeasurementChemicalProfileProjector::isChemical);
  }
}
