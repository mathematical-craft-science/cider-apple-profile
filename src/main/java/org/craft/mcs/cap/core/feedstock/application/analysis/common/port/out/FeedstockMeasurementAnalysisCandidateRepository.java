package org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.jspecify.annotations.NonNull;


/** Supplies individual historical Feedstock measurement sets to analysis scenarios. */
public interface FeedstockMeasurementAnalysisCandidateRepository {

  @NonNull
  List<@NonNull FeedstockMeasurementView> findCandidates(
      @NonNull FeedstockMeasurementAnalysisCandidateQuery query
  );
}
