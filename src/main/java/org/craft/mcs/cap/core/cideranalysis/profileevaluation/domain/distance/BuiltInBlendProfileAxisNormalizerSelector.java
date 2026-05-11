package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.util.EnumMap;
import java.util.Map;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneThresholds;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Selects one of the built-in blend/evaluation axis normalization strategies.
 */
public final class BuiltInBlendProfileAxisNormalizerSelector implements BlendProfileAxisNormalizerSelector {

  private final @NonNull Map<@NonNull BlendProfileAxisNormalizerCode, @NonNull BlendProfileAxisNormalizer> normalizers;

  public BuiltInBlendProfileAxisNormalizerSelector(
      @NonNull SugarAcidityZoneThresholds thresholds
  ) {
    requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
    final var builtInNormalizers = new EnumMap<BlendProfileAxisNormalizerCode, BlendProfileAxisNormalizer>(
        BlendProfileAxisNormalizerCode.class
    );
    builtInNormalizers.put(
        BlendProfileAxisNormalizerCode.MIN_MAX,
        MinMaxBlendProfileAxisNormalizer.fullCiderProfileRange()
    );
    builtInNormalizers.put(
        BlendProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        EffectiveRangeBlendProfileAxisNormalizer.ciderBlendingDefaults()
    );
    builtInNormalizers.put(
        BlendProfileAxisNormalizerCode.THRESHOLD_SEGMENT,
        ThresholdSegmentBlendProfileAxisNormalizer.from(thresholds)
    );
    builtInNormalizers.put(
        BlendProfileAxisNormalizerCode.SENSITIVITY_UNIT,
        SensitivityUnitBlendProfileAxisNormalizer.ciderBlendingDefaults()
    );
    this.normalizers = Map.copyOf(builtInNormalizers);
  }

  @Override
  public @NonNull BlendProfileAxisNormalizer select(
      @NonNull BlendProfileAxisNormalizerCode code
  ) {
    requireNonNull(code, "blend profile axis normalizer code cannot be null");
    final var normalizer = normalizers.get(code);
    if (normalizer == null) { // NOSONAR
      throw new IllegalArgumentException("No blend profile axis normalizer registered for code: " + code);
    }
    return normalizer;
  }
}
