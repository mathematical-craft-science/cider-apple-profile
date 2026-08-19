package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.jspecify.annotations.NonNull;


/** Selects Feedstock subjects eligible for a current-profile page. */
public interface FeedstockCurrentProfilePageSubjectQuery {

  @NonNull
  List<@NonNull FeedstockCurrentProfileSubject> findSubjects(
      @NonNull FeedstockCurrentProfilePageQuery query
  );
}
