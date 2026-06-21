package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockCompositionCandidateView(
    @NonNull List<@NonNull FeedstockCompositionSourceView> sources,
    @NonNull SugarAcidityProfilePoint profile,
    @Nullable SugarAcidityAxisPoint yeastAssimilableNitrogen,
    @Nullable VolumeValue totalVolume,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    @NonNull ProfileTargetDistance targetDistance
) {

  public FeedstockCompositionCandidateView {
    sources = List.copyOf(requireNonNull(sources, "feedstock composition sources cannot be null"));
    requireNonNull(profile, "composition profile cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "composition warnings cannot be null"));
    requireNonNull(targetDistance, "composition target distance cannot be null");
  }
}
