package org.craft.mcs.cap.core.production.application.usecase;

import java.util.List;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProfileView;
import org.craft.mcs.cap.core.production.application.model.ProductionCurrentProfileView;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Overlays current Production measurement evidence on the immutable initial profile snapshot. */
@Component
public final class ProductionCurrentProfileProjector {

  public @NonNull ProductionCurrentProfileView project(
      @NonNull ProductionProfileSnapshot initialProfile,
      @Nullable ProductionMeasurementProfileView measuredProfile
  ) {
    requireNonNull(initialProfile, "production initial profile cannot be null");
    final var initial = toChemicalProfile(initialProfile);
    if (measuredProfile == null) {
      return new ProductionCurrentProfileView(initial, List.of());
    }
    return new ProductionCurrentProfileView(
        overlay(initial, measuredProfile.chemicalProfile()),
        measuredProfile.evidence()
    );
  }

  private static @NonNull ChemicalProfile toChemicalProfile(
      @NonNull ProductionProfileSnapshot snapshot
  ) {
    final var builder = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(snapshot.specificGravity())))
        .titratableAcidity(PointCharacteristicValue.of(snapshot.titratableAcidity()));
    if (snapshot.tannin() != null) {
      builder.tannin(PointCharacteristicValue.of(snapshot.tannin())); // NOSONAR
    }
    if (snapshot.yeastAssimilableNitrogen() != null) {
      builder.yeastAssimilableNitrogen(PointCharacteristicValue.of(snapshot.yeastAssimilableNitrogen())); // NOSONAR
    }
    return builder.build();
  }

  private static @NonNull ChemicalProfile overlay(
      @NonNull ChemicalProfile initial,
      @NonNull ChemicalProfile measured
  ) {
    final var builder = ChemicalProfile.builder();
    measured.sugar().or(initial::sugar).ifPresent(builder::sugar);
    measured.ph().or(initial::ph).ifPresent(builder::ph);
    measured.titratableAcidity()
        .or(initial::titratableAcidity)
        .ifPresent(builder::titratableAcidity);
    measured.tannin().or(initial::tannin).ifPresent(builder::tannin);
    measured.yeastAssimilableNitrogen()
        .or(initial::yeastAssimilableNitrogen)
        .ifPresent(builder::yeastAssimilableNitrogen);
    return builder.build();
  }
}
