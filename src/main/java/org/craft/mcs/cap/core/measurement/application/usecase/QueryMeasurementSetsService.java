package org.craft.mcs.cap.core.measurement.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetNotFoundException;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetViewQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads current measurement-set revisions after enforcing subject visibility. */
@Component
@RequiredArgsConstructor
public final class QueryMeasurementSetsService implements QueryMeasurementSetsUseCase {

  private final @NonNull MeasurementSetViewQuery viewQuery;
  private final @NonNull MeasurementSubjectAccessControl subjectAccessControl;

  @Override
  public @NonNull MeasurementSetView get(@NonNull MeasurementSetId measurementSetId) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    final var view = viewQuery.findById(measurementSetId)
        .orElseThrow(() -> new MeasurementSetNotFoundException(measurementSetId));
    subjectAccessControl.requireReadAccess(view.subject());
    return view;
  }

  @Override
  public @NonNull List<@NonNull MeasurementSetView> list(
      @NonNull MeasurementSubjectReference subject
  ) {
    requireNonNull(subject, "measurement subject cannot be null");
    final var workspaceId = subjectAccessControl.requireReadAccess(subject);
    return viewQuery.findBySubject(workspaceId, subject);
  }

  @Override
  public @NonNull List<@NonNull MeasurementSetView> listRevisions(
      @NonNull MeasurementSetId measurementSetId
  ) {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    final var revisions = viewQuery.findRevisionsById(measurementSetId);
    if (revisions.isEmpty()) {
      throw new MeasurementSetNotFoundException(measurementSetId);
    }
    subjectAccessControl.requireReadAccess(revisions.getFirst().subject());
    return revisions;
  }
}
