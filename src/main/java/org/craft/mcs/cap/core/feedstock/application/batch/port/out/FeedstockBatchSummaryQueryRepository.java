package org.craft.mcs.cap.core.feedstock.application.batch.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read-model repository for feedstock batch list scenarios. */
public interface FeedstockBatchSummaryQueryRepository {

  @NonNull List<@NonNull FeedstockBatchSummary> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId);
}
