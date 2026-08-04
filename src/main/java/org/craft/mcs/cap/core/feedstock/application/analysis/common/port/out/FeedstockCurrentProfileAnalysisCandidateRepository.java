package org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;


/** Supplies state-aware current Feedstock profiles to batch analysis scenarios. */
public interface FeedstockCurrentProfileAnalysisCandidateRepository {

  @NonNull
  List<@NonNull FeedstockCurrentProfileView> findCandidates(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  );
}
