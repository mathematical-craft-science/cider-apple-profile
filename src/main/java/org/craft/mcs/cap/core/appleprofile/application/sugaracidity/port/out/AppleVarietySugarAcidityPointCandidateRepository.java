package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.jspecify.annotations.NonNull;


/**
 * Read-side source of apple variety reference profiles for sugar-acidity analysis.
 *
 * <p>The returned {@link AppleVariety} objects are used as catalog reference profiles for
 * projection and scoring, not as UI read models.
 */
public interface AppleVarietySugarAcidityPointCandidateRepository {

  @NonNull
  List<@NonNull AppleVariety> findCandidates(@NonNull AppleVarietySugarAcidityPointCandidateQuery query);
}
