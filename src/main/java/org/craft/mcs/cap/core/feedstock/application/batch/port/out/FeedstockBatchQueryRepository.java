package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/**
 * Read-side repository for feedstock batch summaries.
 */
public interface FeedstockBatchQueryRepository {

  @NonNull
  List<@NonNull FeedstockBatchSummary> findSummariesByCiderMakerId(@NonNull CiderMakerId ciderMakerId);
}
