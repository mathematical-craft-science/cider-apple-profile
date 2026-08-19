package org.craft.mcs.cap.core.feedstock.planning.application.candidate.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidateQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidateSnapshot;
import org.jspecify.annotations.NonNull;

/** Supplies Feedstock candidate snapshots through the batch-context anti-corruption boundary. */
public interface FeedstockPlanningCandidateProvider {

  @NonNull
  List<@NonNull FeedstockPlanningCandidateSnapshot> findCandidates(
      @NonNull FeedstockPlanningCandidateQuery query
  );
}
