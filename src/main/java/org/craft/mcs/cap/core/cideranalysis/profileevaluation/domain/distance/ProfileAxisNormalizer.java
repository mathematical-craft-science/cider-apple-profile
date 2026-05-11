package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public interface ProfileAxisNormalizer {

  @NonNull
  BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  );
}
