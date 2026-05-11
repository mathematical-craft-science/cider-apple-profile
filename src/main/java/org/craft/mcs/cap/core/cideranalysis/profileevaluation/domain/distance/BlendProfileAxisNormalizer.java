package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public interface BlendProfileAxisNormalizer {

  @NonNull
  BigDecimal normalize(
      @NonNull BlendProfileAxis axis,
      @NonNull BigDecimal value
  );
}
