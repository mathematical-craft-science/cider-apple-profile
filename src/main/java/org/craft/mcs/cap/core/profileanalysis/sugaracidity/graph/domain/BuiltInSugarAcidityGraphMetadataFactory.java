package org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.BuiltInCiderProfileAxisBoundaries;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.EllipseSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneRange;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Builds sugar-acidity plot metadata from the active zone geometry profile.
 */
public final class BuiltInSugarAcidityGraphMetadataFactory {

  private BuiltInSugarAcidityGraphMetadataFactory() {
  }

  public static @NonNull SugarAcidityGraphMetadata from(
      @NonNull SugarAcidityZoneGeometryProfile profile
  ) {
    requireNonNull(profile, "sugar-acidity zone geometry profile cannot be null");
    return new SugarAcidityGraphMetadata(
        new SugarAcidityGraphAxis(
            "SG",
            "Specific gravity",
            BeverageUnits.label(BeverageUnits.SG),
            BuiltInCiderProfileAxisBoundaries.SUGAR_MIN,
            BuiltInCiderProfileAxisBoundaries.SUGAR_MAX,
            List.of(
                BuiltInCiderProfileAxisBoundaries.SUGAR_LOW_MEDIUM_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.SUGAR_MEDIUM_HIGH_BOUNDARY
            )
        ),
        new SugarAcidityGraphAxis(
            "TA",
            "Total acidity",
            BeverageUnits.label(BeverageUnits.TA),
            BuiltInCiderProfileAxisBoundaries.ACIDITY_MIN,
            BuiltInCiderProfileAxisBoundaries.ACIDITY_MAX,
            List.of(
                BuiltInCiderProfileAxisBoundaries.ACIDITY_LOW_MEDIUM_BOUNDARY,
                BuiltInCiderProfileAxisBoundaries.ACIDITY_MEDIUM_HIGH_BOUNDARY
            )
        ),
        profile.zones().stream()
            .map(BuiltInSugarAcidityGraphMetadataFactory::graphZone)
            .toList()
    );
  }

  private static @NonNull SugarAcidityGraphZone graphZone(
      @NonNull SugarAcidityZoneGeometry geometry
  ) {
    return switch (geometry) {
      case RectangleSugarAcidityZoneGeometry(var zone, var sugarRange, var acidityRange) ->
          rectangleGraphZone(zone, sugarRange, acidityRange);
      case EllipseSugarAcidityZoneGeometry(
          var zone,
          var sugarCenter,
          var acidityCenter,
          var sugarRadius,
          var acidityRadius
      ) -> new EllipseSugarAcidityGraphZone(
          zone,
          sugarCenter,
          acidityCenter,
          sugarRadius,
          acidityRadius
      );
    };
  }

  private static @NonNull RectangleSugarAcidityGraphZone rectangleGraphZone(
      @NonNull SugarAcidityZone zone,
      @NonNull SugarAcidityZoneRange sugarRange,
      @NonNull SugarAcidityZoneRange acidityRange
  ) {
    return new RectangleSugarAcidityGraphZone(
        zone,
        new SugarAcidityGraphRange(sugarRange.min(), sugarRange.max()),
        new SugarAcidityGraphRange(acidityRange.min(), acidityRange.max())
    );
  }
}
