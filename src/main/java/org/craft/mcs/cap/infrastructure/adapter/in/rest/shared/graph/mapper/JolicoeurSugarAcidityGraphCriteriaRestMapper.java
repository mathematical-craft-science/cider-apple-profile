package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphCriteria;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphCriteria;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphCriteria;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.Nullable;


/** Maps Jolicoeur graph filter transport values to scenario-specific application criteria. */
public final class JolicoeurSugarAcidityGraphCriteriaRestMapper {

  private JolicoeurSugarAcidityGraphCriteriaRestMapper() {
  }

  public static @Nullable FeedstockSugarAcidityGraphCriteria toFeedstockGraphCriteria(
      @Nullable JolicoeurSugarAcidityZone zone,
      @Nullable Boolean plottable
  ) {
    return zone == null && plottable == null
        ? null
        : new FeedstockSugarAcidityGraphCriteria(
            zone == null // NOSONAR
                ? Set.of()
                : Set.of(FeedstockSugarAcidityGraphView.Zone.valueOf(zone.name())),
            plottable
        );
  }

  public static @Nullable AppleSugarAcidityGraphCriteria toAppleGraphCriteria(
      @Nullable JolicoeurSugarAcidityZone zone,
      @Nullable Boolean plottable
  ) {
    return zone == null && plottable == null
        ? null
        : new AppleSugarAcidityGraphCriteria(
            zone == null // NOSONAR
                ? Set.of()
                : Set.of(AppleSugarAcidityGraphView.Zone.valueOf(zone.name())),
            plottable
        );
  }

  public static @Nullable FeedstockPlanningGraphCriteria toFeedstockPlanningGraphCriteria(
      @Nullable JolicoeurSugarAcidityZone zone,
      @Nullable Boolean plottable
  ) {
    return zone == null && plottable == null
        ? null
        : new FeedstockPlanningGraphCriteria(
            zone == null // NOSONAR
                ? Set.of()
                : Set.of(FeedstockPlanningGraphView.Zone.valueOf(zone.name())),
            plottable
        );
  }

}
