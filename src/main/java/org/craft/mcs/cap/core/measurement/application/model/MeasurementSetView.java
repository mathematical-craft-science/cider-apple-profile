package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Provider-neutral read model of the current revision of a logical measurement set. */
public record MeasurementSetView(
    @NonNull MeasurementSetId measurementSetId,
    @NonNull WorkspaceId workspaceId,
    @NonNull MeasurementSubjectReference subject,
    @NonNull MeasurementSetLifecycle lifecycle,
    long version,
    @NonNull MeasurementSetRevisionId revisionId,
    @NonNull MeasurementSetRevisionNumber revisionNumber,
    @NonNull MaterialStateId materialStateId,
    @NonNull MeasurementSetName name,
    @Nullable String note,
    @Nullable MeasurementCorrectionReason correctionReason,
    @NonNull Instant revisionCreatedAt,
    @NonNull ActorReference actor,
    @NonNull List<@NonNull MeasurementRecordView> records,
    @NonNull Instant createdAt,
    @NonNull Instant updatedAt
) {

  public MeasurementSetView {
    requireNonNull(measurementSetId, "measurement set view id cannot be null");
    requireNonNull(workspaceId, "measurement set view workspace id cannot be null");
    requireNonNull(subject, "measurement set view subject cannot be null");
    requireNonNull(lifecycle, "measurement set view lifecycle cannot be null");
    requireNonNull(revisionId, "measurement set view revision id cannot be null");
    requireNonNull(revisionNumber, "measurement set view revision number cannot be null");
    requireNonNull(materialStateId, "measurement set view material state id cannot be null");
    requireNonNull(name, "measurement set view name cannot be null");
    requireNonNull(revisionCreatedAt, "measurement set view revision creation time cannot be null");
    requireNonNull(actor, "measurement set view actor cannot be null");
    records = List.copyOf(requireNonNull(records, "measurement set view records cannot be null"));
    requireNonNull(createdAt, "measurement set view creation time cannot be null");
    requireNonNull(updatedAt, "measurement set view update time cannot be null");
    if (version < 0) {
      throw new ApplicationValidationException("measurement set view version cannot be negative");
    }
  }
}
