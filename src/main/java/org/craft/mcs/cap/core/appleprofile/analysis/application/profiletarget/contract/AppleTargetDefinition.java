package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract;

import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView.Zone;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Target selected for Apple catalog candidate analysis. */
public record AppleTargetDefinition(
    @NonNull SugarAcidityTarget sugarAcidity,
    @Nullable MeasuredValueRange<TanninValue> tannin
) {

  public AppleTargetDefinition {
    requireNonNull(sugarAcidity, "apple analysis SG/TA target cannot be null");
  }

  public sealed interface SugarAcidityTarget permits PredefinedTarget, CustomRectangleTarget {
  }

  public record PredefinedTarget(@NonNull Zone zone) implements SugarAcidityTarget {

    public PredefinedTarget {
      requireNonNull(zone, "apple analysis target zone cannot be null");
    }
  }

  public record CustomRectangleTarget(
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravity,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidity
  ) implements SugarAcidityTarget {

    public CustomRectangleTarget {
      requireNonNull(specificGravity, "apple target specific gravity range cannot be null");
      requireNonNull(titratableAcidity, "apple target titratable acidity range cannot be null");
    }
  }

}
