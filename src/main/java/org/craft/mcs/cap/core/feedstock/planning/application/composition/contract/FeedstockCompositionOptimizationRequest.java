package org.craft.mcs.cap.core.feedstock.planning.application.composition.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveFunction;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Feedstock-owned immutable problem sent through the formulation ACL. */
public record FeedstockCompositionOptimizationRequest(
    @NonNull List<@NonNull Source> sources,
    @NonNull TanninPolicy tanninPolicy,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable MeasuredValueRange<TanninValue> tanninTarget,
    @NonNull Scoring scoring,
    @NonNull List<@NonNull Criterion> criteria,
    @NonNull Settings settings,
    @NonNull Mode mode,
    @NonNull Algorithm algorithm,
    @NonNull Calculator calculator,
    @Nullable VolumeValue targetTotalVolume
) {

  public FeedstockCompositionOptimizationRequest {
    sources = List.copyOf(requireNonNull(sources, "feedstock optimization sources cannot be null"));
    requireNonNull(tanninPolicy, "feedstock optimization tannin policy cannot be null");
    requireNonNull(target, "feedstock optimization target cannot be null");
    requireNonNull(scoring, "feedstock optimization scoring cannot be null");
    criteria = List.copyOf(requireNonNull(criteria, "feedstock optimization criteria cannot be null"));
    requireNonNull(settings, "feedstock optimization settings cannot be null");
    requireNonNull(mode, "feedstock optimization mode cannot be null");
    requireNonNull(algorithm, "feedstock optimization algorithm cannot be null");
    requireNonNull(calculator, "feedstock optimization calculator cannot be null");
  }

  public record Source(
      @NonNull FeedstockBatchId batchId,
      @NonNull Characteristics characteristics,
      @Nullable VolumeValue availableVolume
  ) {

    public Source {
      requireNonNull(batchId, "feedstock optimization source batch id cannot be null");
      requireNonNull(characteristics, "feedstock optimization source characteristics cannot be null");
    }
  }

  public record Characteristics(
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
      @Nullable CharacteristicValue<TanninValue> tannin,
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  ) {

    public Characteristics {
      requireNonNull(specificGravity, "feedstock optimization source SG cannot be null");
      requireNonNull(titratableAcidity, "feedstock optimization source TA cannot be null");
    }
  }

  public record Criterion(
      @Nullable YeastAssimilableNitrogenValue minimumYan,
      @Nullable YeastAssimilableNitrogenValue maximumYan,
      @NonNull BigDecimal weight,
      @NonNull MissingPolicy missingPolicy,
      @NonNull BigDecimal missingPenalty
  ) {

    public Criterion {
      requireNonNull(weight, "feedstock optimization criterion weight cannot be null");
      requireNonNull(missingPolicy, "feedstock optimization criterion missing policy cannot be null");
      requireNonNull(missingPenalty, "feedstock optimization missing penalty cannot be null");
    }
  }

  public record Settings(int stepPercent, int limit) {

    private static final int DEFAULT_STEP_PERCENT = 5;
    private static final int DEFAULT_LIMIT = 10;

    public Settings {
      if (stepPercent < 1 || stepPercent > 50 || 100 % stepPercent != 0) {
        throw new ApplicationValidationException(
            "feedstock optimization step percent must be between 1 and 50 and divide 100 exactly"
        );
      }
      if (limit < 1 || limit > 100) {
        throw new ApplicationValidationException(
            "feedstock optimization candidate limit must be between 1 and 100"
        );
      }
    }

    public static @NonNull Settings defaults() {
      return new Settings(DEFAULT_STEP_PERCENT, DEFAULT_LIMIT);
    }
  }

  public record Scoring(
      @NonNull Normalizer normalizer,
      @NonNull ObjectiveFunction objectiveFunction,
      @NonNull ObjectiveWeights weights
  ) {

    public Scoring {
      requireNonNull(normalizer, "feedstock optimization normalizer cannot be null");
      requireNonNull(objectiveFunction, "feedstock optimization objective function cannot be null");
      requireNonNull(weights, "feedstock optimization target weights cannot be null");
    }
  }

  public enum TanninPolicy { CALCULATE_WHEN_COMPLETE, REQUIRE_TANNIN, IGNORE_TANNIN }
  public enum MissingPolicy { IGNORE_IF_MISSING, PENALIZE_IF_MISSING, EXCLUDE_IF_MISSING }
  public enum Mode { PROPORTION, ABSOLUTE_VOLUME }
  public enum Algorithm {
    DISCRETIZED_BOUNDED_SEARCH,
    LINEAR_PROGRAMMING,
    QUADRATIC_PROGRAMMING,
    MIXED_INTEGER_PROGRAMMING,
    LOCAL_SEARCH,
    EVOLUTIONARY_HEURISTIC,
    NONLINEAR_OPTIMIZATION
  }
  public enum Calculator { MASS_BALANCE }
}
