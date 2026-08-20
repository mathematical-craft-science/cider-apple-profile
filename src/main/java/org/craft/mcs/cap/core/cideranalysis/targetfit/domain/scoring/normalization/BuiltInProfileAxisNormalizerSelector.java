package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Selects one of the built-in profile evaluation axis normalization strategies.
 *
 * <p>The built-ins are intentionally domain-specific:
 * <ul>
 *   <li>{@link ProfileAxisNormalizerCode#MIN_MAX}: full cider profile axis range;</li>
 *   <li>{@link ProfileAxisNormalizerCode#EFFECTIVE_RANGE}: practical cider blending range;</li>
 *   <li>{@link ProfileAxisNormalizerCode#THRESHOLD_SEGMENT}: SG/TA/tannin scoring segments;</li>
 *   <li>{@link ProfileAxisNormalizerCode#SENSITIVITY_UNIT}: expert-calibrated units.</li>
 * </ul>
 */
public final class BuiltInProfileAxisNormalizerSelector implements ProfileAxisNormalizerSelector {

  private final @NonNull Map<@NonNull ProfileAxisNormalizerCode, @NonNull ProfileAxisNormalizer> normalizers;

  public BuiltInProfileAxisNormalizerSelector() {
    final var builtInNormalizers = new EnumMap<ProfileAxisNormalizerCode, ProfileAxisNormalizer>(
        ProfileAxisNormalizerCode.class
    );
    builtInNormalizers.put(
        ProfileAxisNormalizerCode.MIN_MAX,
        MinMaxProfileAxisNormalizer.fullCiderProfileRange()
    );
    builtInNormalizers.put(
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        EffectiveRangeProfileAxisNormalizer.ciderBlendingDefaults()
    );
    builtInNormalizers.put(
        ProfileAxisNormalizerCode.THRESHOLD_SEGMENT,
        ThresholdSegmentProfileAxisNormalizer.defaultScoringCalibration()
    );
    builtInNormalizers.put(
        ProfileAxisNormalizerCode.SENSITIVITY_UNIT,
        new SensitivityUnitProfileAxisNormalizer(
            ProfileAxisSensitivityUnits.ciderBlendingV1()
        )
    );
    this.normalizers = Map.copyOf(builtInNormalizers);
  }

  @Override
  public @NonNull ProfileAxisNormalizer select(
      @NonNull ProfileAxisNormalizerCode code
  ) {
    requireNonNull(code, "profile axis normalizer code cannot be null");
    return Optional.ofNullable(normalizers.get(code))
        .orElseThrow(() -> new DomainRuleViolationException("No profile axis normalizer registered for code: " + code));
  }
}
