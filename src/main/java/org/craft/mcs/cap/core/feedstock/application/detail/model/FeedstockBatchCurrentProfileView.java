package org.craft.mcs.cap.core.feedstock.application.detail.model;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
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
    @Nullable FeedstockMeasurementSummary latestMeasurement,
    @Nullable FeedstockCurrentProfileSummaryView currentProfileSummary,
    @NonNull FeedstockCurrentProfileAssessmentView currentProfileAssessment
) {

  public FeedstockBatchCurrentProfileView {
    requireNonNull(currentProfileAssessment, "feedstock current profile assessment cannot be null");
  }
}
