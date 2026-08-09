package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
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

  private static final @NonNull CursorScope CURRENT_SCOPE = CursorScope.of(
      "feedstock.current-sugar-acidity.measured-at-desc.v2"
  );

  private final @NonNull CursorTokenCodec cursorTokenCodec;

  public @Nullable FeedstockAnalysisCandidatePagePosition decodeCurrent(
      @Nullable String cursor,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    return decode(cursor, CURRENT_SCOPE, workspaceId, filters, graphCriteria);
  }

  public @Nullable String encodeCurrent(
      @Nullable FeedstockAnalysisCandidatePagePosition position,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    return encode(position, CURRENT_SCOPE, workspaceId, filters, graphCriteria);
  }

  private @Nullable FeedstockAnalysisCandidatePagePosition decode(
      @Nullable String cursor,
      @NonNull CursorScope scope,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    requireNonNull(scope, "cursor scope cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    return cursorTokenCodec.decode(
        cursor.strip(),
        binding(scope, workspaceId, filters, graphCriteria),
        FeedstockAnalysisCandidatePagePosition.class
    );
  }

  private @Nullable String encode(
      @Nullable FeedstockAnalysisCandidatePagePosition position,
      @NonNull CursorScope scope,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    requireNonNull(scope, "cursor scope cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    return position == null
        ? null
        : cursorTokenCodec.encode(binding(scope, workspaceId, filters, graphCriteria), position);
  }

  private static @NonNull CursorBinding binding(
      @NonNull CursorScope scope,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    return CursorBinding.boundTo(
        scope,
        "workspace=" + workspaceId.value(),
        "batches=" + batches(filters),
        "season=" + season(filters),
        "zones=" + zones(graphCriteria),
        "plottable=" + plottable(graphCriteria)
    );
  }

  private static @NonNull String batches(@NonNull FeedstockAnalysisCandidateFilters filters) {
    return filters.batch() == null
        ? ""
        : filters.batch().batchIds().stream() // NOSONAR
            .map(batchId -> batchId.value().toString())
            .sorted()
            .collect(Collectors.joining(","));
  }

  private static @NonNull String season(@NonNull FeedstockAnalysisCandidateFilters filters) {
    return filters.season() == null
        ? ""
        : filters.season().startsOn() + ":" + filters.season().endsOn(); // NOSONAR
  }

  private static @NonNull String zones(@Nullable JolicoeurSugarAcidityGraphCriteria graph) {
    final JolicoeurSugarAcidityZoneCriteria criteria = graph == null ? null : graph.zone();
    return criteria == null
        ? ""
        : criteria.zones().stream()
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(","));
  }

  private static @NonNull String plottable(@Nullable JolicoeurSugarAcidityGraphCriteria graph) {
    return graph == null || graph.plottable() == null ? "" : graph.plottable().toString(); // NOSONAR
  }
}
