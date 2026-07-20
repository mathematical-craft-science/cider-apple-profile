package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorBinding;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorScope;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorTokenCodec;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Maps opaque REST cursor tokens to typed feedstock sugar-acidity page positions and back. */
@Component
@RequiredArgsConstructor
public final class FeedstockSugarAcidityCursorAdapter {

  private static final @NonNull CursorScope HISTORICAL_SCOPE = CursorScope.of(
      "feedstock.measurement-sugar-acidity.measured-at-asc.v1"
  );
  private static final @NonNull CursorScope CURRENT_SCOPE = CursorScope.of(
      "feedstock.current-sugar-acidity.measured-at-desc.v1"
  );

  private final @NonNull CursorTokenCodec cursorTokenCodec;

  public @Nullable FeedstockMeasurementSugarAcidityPagePosition decodeHistorical(
      @Nullable String cursor,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    return decode(cursor, HISTORICAL_SCOPE, workspaceId, filters);
  }

  public @Nullable String encodeHistorical(
      @Nullable FeedstockMeasurementSugarAcidityPagePosition position,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    return encode(position, HISTORICAL_SCOPE, workspaceId, filters);
  }

  public @Nullable FeedstockMeasurementSugarAcidityPagePosition decodeCurrent(
      @Nullable String cursor,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    return decode(cursor, CURRENT_SCOPE, workspaceId, filters);
  }

  public @Nullable String encodeCurrent(
      @Nullable FeedstockMeasurementSugarAcidityPagePosition position,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    return encode(position, CURRENT_SCOPE, workspaceId, filters);
  }

  private @Nullable FeedstockMeasurementSugarAcidityPagePosition decode(
      @Nullable String cursor,
      @NonNull CursorScope scope,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    requireNonNull(scope, "cursor scope cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    return cursorTokenCodec.decode(
        cursor.strip(),
        binding(scope, workspaceId, filters),
        FeedstockMeasurementSugarAcidityPagePosition.class
    );
  }

  private @Nullable String encode(
      @Nullable FeedstockMeasurementSugarAcidityPagePosition position,
      @NonNull CursorScope scope,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    requireNonNull(scope, "cursor scope cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    return position == null
        ? null
        : cursorTokenCodec.encode(binding(scope, workspaceId, filters), position);
  }

  private static @NonNull CursorBinding binding(
      @NonNull CursorScope scope,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockMeasurementPointFilters filters
  ) {
    return CursorBinding.boundTo(
        scope,
        "workspace=" + workspaceId.value(),
        "batches=" + batches(filters),
        "season=" + season(filters),
        "zones=" + zones(filters.sugarAcidityZone()),
        "capability=" + capability(filters.capability())
    );
  }

  private static @NonNull String batches(@NonNull FeedstockMeasurementPointFilters filters) {
    return filters.batch() == null
        ? ""
        : filters.batch().batchIds().stream() // NOSONAR
            .map(batchId -> batchId.value().toString())
            .sorted()
            .collect(Collectors.joining(","));
  }

  private static @NonNull String season(@NonNull FeedstockMeasurementPointFilters filters) {
    return filters.season() == null
        ? ""
        : filters.season().startsOn() + ":" + filters.season().endsOn(); // NOSONAR
  }

  private static @NonNull String zones(@Nullable SugarAcidityZoneCriteria criteria) {
    return criteria == null
        ? ""
        : criteria.zones().stream()
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(","));
  }

  private static @NonNull String capability(@Nullable ChemicalProfileCapabilityCriteria criteria) {
    return criteria == null || criteria.capable() == null
        ? ""
        : criteria.capability().name() + ":" + criteria.capable();
  }
}
