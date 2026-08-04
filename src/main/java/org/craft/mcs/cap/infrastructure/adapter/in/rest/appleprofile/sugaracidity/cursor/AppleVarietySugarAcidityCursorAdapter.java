package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor;

import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.profileanalysis.capability.application.model.ProfileAnalysisCapabilityCriteria;
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
      "appleprofile.sugar-acidity-points.name-asc.v1"
  );

  private final @NonNull CursorTokenCodec cursorTokenCodec;

  public @Nullable AppleVarietyCatalogPagePosition decode(
      @Nullable String cursor,
      @NonNull AppleVarietyAnalysisFilters filters
  ) {
    requireNonNull(filters, "apple variety point filters cannot be null");
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    return cursorTokenCodec.decode(
        cursor.strip(),
        binding(filters),
        AppleVarietyCatalogPagePosition.class
    );
  }

  public @Nullable String encode(
      @Nullable AppleVarietyCatalogPagePosition position,
      @NonNull AppleVarietyAnalysisFilters filters
  ) {
    requireNonNull(filters, "apple variety point filters cannot be null");
    return position == null ? null : cursorTokenCodec.encode(binding(filters), position);
  }

  private static @NonNull CursorBinding binding(@NonNull AppleVarietyAnalysisFilters filters) {
    return CursorBinding.boundTo(
        CURSOR_SCOPE,
        "name=" + nameContains(filters.nameContains()),
        "zones=" + zones(filters.sugarAcidityZone()),
        "capability=" + capability(filters.capability())
    );
  }

  private static @NonNull String nameContains(@Nullable TextSearchCriteria criteria) {
    return criteria == null ? "" : criteria.query().toLowerCase(Locale.ROOT);
  }

  private static @NonNull String zones(@Nullable JolicoeurSugarAcidityZoneCriteria criteria) {
    return criteria == null
        ? ""
        : criteria.zones().stream()
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(","));
  }

  private static @NonNull String capability(@Nullable ProfileAnalysisCapabilityCriteria criteria) {
    return criteria == null || criteria.capable() == null
        ? ""
        : criteria.capability().name() + ":" + criteria.capable();
  }
}
