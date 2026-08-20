package org.craft.mcs.cap.core.feedstock.planning.application.composition.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Algorithm;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Calculator;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Mode;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Settings;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveFunction;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Feedstock-owned read model of a formulation optimization run. */
public record FeedstockCompositionOptimizationView(
    @NonNull Mode mode,
    @NonNull Algorithm algorithm,
    @NonNull Calculator calculator,
    @NonNull Normalizer normalizer,
    @NonNull ObjectiveFunction objectiveFunction,
    @NonNull Settings settings,
    int evaluatedCandidates,
    @NonNull List<@NonNull Candidate> candidates
) {

  public FeedstockCompositionOptimizationView {
    requireNonNull(mode, "feedstock optimization mode cannot be null");
    requireNonNull(algorithm, "feedstock optimization algorithm cannot be null");
    requireNonNull(calculator, "feedstock optimization calculator cannot be null");
    requireNonNull(normalizer, "feedstock optimization normalizer cannot be null");
    requireNonNull(objectiveFunction, "feedstock optimization objective function cannot be null");
    requireNonNull(settings, "feedstock optimization settings cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "feedstock optimization candidates cannot be null"));
  }

  public record Candidate(
      @NonNull Composition composition,
      @NonNull FeedstockTargetDistanceView targetDistance,
      @NonNull Objective objective,
      @NonNull PotentialAbv potentialAbv
  ) {

    public Candidate {
      requireNonNull(composition, "feedstock optimized composition cannot be null");
      requireNonNull(targetDistance, "feedstock optimized target distance cannot be null");
      requireNonNull(objective, "feedstock optimization objective cannot be null");
      requireNonNull(potentialAbv, "feedstock optimization potential ABV cannot be null");
    }
  }

  public record Composition(
      @NonNull List<@NonNull Source> sources,
      @NonNull Characteristics characteristics,
      @Nullable VolumeValue totalVolume,
      @NonNull List<@NonNull Warning> warnings
  ) {

    public Composition {
      sources = List.copyOf(requireNonNull(sources, "optimized composition sources cannot be null"));
      requireNonNull(characteristics, "optimized composition characteristics cannot be null");
      warnings = List.copyOf(requireNonNull(warnings, "optimized composition warnings cannot be null"));
    }
  }

  public record Source(
      @NonNull FeedstockBatchId batchId,
      @NonNull Weight weight
  ) {

    public Source {
      requireNonNull(batchId, "optimized composition source batch id cannot be null");
      requireNonNull(weight, "optimized composition source weight cannot be null");
    }

    public @Nullable VolumeValue requestedVolume() {
      return switch (weight) {
        case AbsoluteVolume(var volume) -> volume;
        case Proportion _ -> null;
      };
    }

    public @Nullable BigDecimal proportion() {
      return switch (weight) {
        case AbsoluteVolume _ -> null;
        case Proportion(var value) -> value;
      };
    }
  }

  public sealed interface Weight permits AbsoluteVolume, Proportion {
  }

  public record AbsoluteVolume(@NonNull VolumeValue volume) implements Weight {

    public AbsoluteVolume {
      requireNonNull(volume, "optimized composition source volume cannot be null");
    }
  }

  public record Proportion(@NonNull BigDecimal value) implements Weight {

    public Proportion {
      requireNonNull(value, "optimized composition source proportion cannot be null");
    }
  }

  public record Characteristics(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity,
      @Nullable TanninValue tannin,
      @Nullable YeastAssimilableNitrogenValue yeastAssimilableNitrogen
  ) {

    public Characteristics {
      requireNonNull(specificGravity, "optimized composition specific gravity cannot be null");
      requireNonNull(titratableAcidity, "optimized composition titratable acidity cannot be null");
    }
  }

  public record PotentialAbv(
      @NonNull SpecificGravityValue originalGravity,
      @NonNull SpecificGravityValue predictedFinalGravity,
      @NonNull FinalGravitySource predictedFinalGravitySource,
      @NonNull AbvValue potentialAbv,
      @NonNull AbvMethod method,
      @NonNull Confidence confidence
  ) {

    public PotentialAbv {
      requireNonNull(originalGravity, "optimized composition original gravity cannot be null");
      requireNonNull(predictedFinalGravity, "optimized composition predicted final gravity cannot be null");
      requireNonNull(predictedFinalGravitySource, "optimized composition final gravity source cannot be null");
      requireNonNull(potentialAbv, "optimized composition potential ABV cannot be null");
      requireNonNull(method, "optimized composition ABV method cannot be null");
      requireNonNull(confidence, "optimized composition ABV confidence cannot be null");
    }
  }

  public record Objective(
      @Nullable BigDecimal score,
      @NonNull List<@NonNull Penalty> penalties
  ) {

    public Objective {
      penalties = List.copyOf(requireNonNull(penalties, "feedstock optimization penalties cannot be null"));
    }
  }

  public record Penalty(
      @NonNull PenaltyCode code,
      @NonNull PenaltySeverity severity,
      @NonNull BigDecimal value,
      @Nullable MeasurementCharacteristic characteristic,
      @Nullable BigDecimal actualValue,
      @Nullable BigDecimal threshold
  ) {

    public Penalty {
      requireNonNull(code, "feedstock optimization penalty code cannot be null");
      requireNonNull(severity, "feedstock optimization penalty severity cannot be null");
      requireNonNull(value, "feedstock optimization penalty value cannot be null");
    }
  }

  public enum Warning { TANNIN_IGNORED, TANNIN_INCOMPLETE, YEAST_ASSIMILABLE_NITROGEN_INCOMPLETE }
  public enum FinalGravitySource {
    FIXED_DRY_CIDER_DEFAULT,
    YEAST_CHARACTERISTICS,
    FERMENTATION_MODEL,
    FERMENTATION_MEASUREMENTS
  }
  public enum AbvMethod { STANDARD_GRAVITY_DROP }
  public enum Confidence { LOW, MEDIUM, HIGH }
  public enum PenaltyCode {
    MISSING_YEAST_ASSIMILABLE_NITROGEN,
    LOW_YEAST_ASSIMILABLE_NITROGEN,
    HIGH_YEAST_ASSIMILABLE_NITROGEN
  }
  public enum PenaltySeverity { INFO, WARNING, BLOCKING }
}
