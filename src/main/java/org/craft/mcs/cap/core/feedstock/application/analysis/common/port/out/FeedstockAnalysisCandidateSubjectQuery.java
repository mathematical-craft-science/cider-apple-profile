package org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSubject;
import org.jspecify.annotations.NonNull;


/** Selects Feedstock subjects eligible for state-aware analysis projection. */
public interface FeedstockAnalysisCandidateSubjectQuery {

  @NonNull
  List<@NonNull FeedstockCurrentProfileSubject> findCandidates(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  );
}
