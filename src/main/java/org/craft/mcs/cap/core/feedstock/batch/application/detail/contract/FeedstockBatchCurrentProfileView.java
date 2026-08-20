package org.craft.mcs.cap.core.feedstock.batch.application.detail.contract;

import org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSummaryView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Current feedstock profile read model for the detail page.
 *
 * <p>The model is assembled independently of current evidence for each characteristic. It is not
 * an edit model and does not change feedstock aggregate state.
 */
public record FeedstockBatchCurrentProfileView(
    @Nullable FeedstockCurrentProfileSummaryView currentProfileSummary,
    @NonNull FeedstockCurrentProfileAssessmentView currentProfileAssessment
) {

  public FeedstockBatchCurrentProfileView {
    requireNonNull(currentProfileAssessment, "feedstock current profile assessment cannot be null");
  }
}
