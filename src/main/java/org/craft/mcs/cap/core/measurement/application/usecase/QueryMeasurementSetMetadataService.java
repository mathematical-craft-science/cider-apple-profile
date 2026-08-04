package org.craft.mcs.cap.core.measurement.application.usecase;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetMetadataUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetMetadataQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Exposes workspace-scoped measurement-set metadata without leaking persistence details. */
@Component
@RequiredArgsConstructor
public final class QueryMeasurementSetMetadataService implements QueryMeasurementSetMetadataUseCase {

  private final @NonNull MeasurementSetMetadataQuery metadataQuery;

  @Override
  public @NonNull Map<@NonNull MeasurementSetId, @NonNull MeasurementSetMetadataView> findAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull MeasurementSetId> measurementSetIds
  ) {
    requireNonNull(workspaceId, "measurement metadata workspace id cannot be null");
    requireNonNull(measurementSetIds, "measurement set metadata ids cannot be null");
    return metadataQuery.findAll(workspaceId, Set.copyOf(measurementSetIds));
  }
}
