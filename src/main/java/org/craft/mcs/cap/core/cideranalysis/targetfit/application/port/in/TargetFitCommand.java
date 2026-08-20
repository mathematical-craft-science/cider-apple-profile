package org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateReference;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable published input for scoring characteristic snapshots against one cider target. */
public record TargetFitCommand(
    @NonNull TargetObjective objective,
    @NonNull TargetFitScoringConfiguration configuration,
    @NonNull IntervalPolicy intervalPolicy,
    @NonNull List<@NonNull Candidate> candidates
) {

  public TargetFitCommand {
    requireNonNull(objective, "target objective cannot be null");
    requireNonNull(configuration, "target scoring configuration cannot be null");
    requireNonNull(intervalPolicy, "target interval policy cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "target candidates cannot be null"));
    final var distinctReferences = candidates.stream()
        .map(Candidate::reference)
        .distinct()
        .count();
    if (distinctReferences != candidates.size()) {
      throw new ApplicationValidationException("target candidate references must be unique");
    }
  }

  public record Candidate(
      @NonNull TargetFitCandidateReference reference,
      @NonNull Characteristics characteristics
  ) {

    public Candidate {
      requireNonNull(reference, "target candidate reference cannot be null");
      requireNonNull(characteristics, "target candidate characteristics cannot be null");
    }
  }

  public record Characteristics(
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
      @Nullable CharacteristicValue<TanninValue> tannin
  ) {

    public Characteristics {
      requireNonNull(specificGravity, "target specific gravity cannot be null");
      requireNonNull(titratableAcidity, "target titratable acidity cannot be null");
    }
  }

  public record TargetObjective(
      @NonNull TargetArea sugarAcidityTarget,
      @Nullable MeasuredValueRange<TanninValue> tanninTarget
  ) {

    public TargetObjective {
      requireNonNull(sugarAcidityTarget, "SG/TA target cannot be null");
      if (tanninTarget != null && tanninTarget.upperBound() != MeasuredValueRange.UpperBound.INCLUSIVE) {
        throw new ApplicationValidationException("tannin target must include its maximum");
      }
    }
  }

  public sealed interface TargetArea permits PredefinedTarget, CustomRectangleTarget {
  }

  public record PredefinedTarget(
      @NonNull JolicoeurSugarAcidityZoneCode zone
  ) implements TargetArea {

    public PredefinedTarget {
      requireNonNull(zone, "predefined target zone cannot be null");
    }
  }

  public record CustomRectangleTarget(
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravity,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidity
  ) implements TargetArea {

    public CustomRectangleTarget {
      requireNonNull(specificGravity, "target specific gravity range cannot be null");
      requireNonNull(titratableAcidity, "target titratable acidity range cannot be null");
    }
  }

  public enum IntervalPolicy {
    MIDPOINT
  }
}
