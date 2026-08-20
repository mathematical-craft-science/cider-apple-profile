package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization;

import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


/**
 * Maps raw profile axis values to a comparable numerical scale before distance calculation.
 *
 * <p>SG, TA, and tannin have different units and natural ranges, so objective functions must
 * not compare raw values directly. Normalizers are selected by
 * {@link ProfileAxisNormalizerSelector}; callers can choose a normalizer explicitly or let
 * application policies choose one for a specific use case.
 */
public interface ProfileAxisNormalizer {

  @NonNull
  BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  );
}
