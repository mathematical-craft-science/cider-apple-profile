package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor;

import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorBinding;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorScope;
import org.craft.mcs.cap.shared.infrastructure.query.cursor.CursorTokenCodec;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Maps opaque REST cursor tokens to typed apple-variety sugar-acidity page positions and back. */
@Component
@RequiredArgsConstructor
public final class AppleVarietySugarAcidityCursorAdapter {

  private static final @NonNull CursorScope CURSOR_SCOPE = CursorScope.of(
      "appleprofile.sugar-acidity-points.name-asc.v2"
  );

  private final @NonNull CursorTokenCodec cursorTokenCodec;

  public @Nullable AppleVarietyAnalysisPagePosition decode(
      @Nullable String cursor,
      @NonNull AppleVarietyAnalysisFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    requireNonNull(filters, "apple variety point filters cannot be null");
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    return cursorTokenCodec.decode(
        cursor.strip(),
        binding(filters, graphCriteria),
        AppleVarietyAnalysisPagePosition.class
    );
  }

  public @Nullable String encode(
      @Nullable AppleVarietyAnalysisPagePosition position,
      @NonNull AppleVarietyAnalysisFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    requireNonNull(filters, "apple variety point filters cannot be null");
    return position == null ? null : cursorTokenCodec.encode(binding(filters, graphCriteria), position);
  }

  private static @NonNull CursorBinding binding(
      @NonNull AppleVarietyAnalysisFilters filters,
      @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria
  ) {
    return CursorBinding.boundTo(
        CURSOR_SCOPE,
        "name=" + nameContains(filters.nameContains()),
        "zones=" + zones(graphCriteria),
        "plottable=" + plottable(graphCriteria)
    );
  }

  private static @NonNull String nameContains(@Nullable TextSearchCriteria criteria) {
    return criteria == null ? "" : criteria.query().toLowerCase(Locale.ROOT);
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
