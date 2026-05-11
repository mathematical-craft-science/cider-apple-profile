package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;

import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneGeometryProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Resolves predefined targets from domain sugar-acidity zone geometries.
 */
public final class ZoneGeometrySugarAcidityTargetResolver implements SugarAcidityTargetResolver {

  private final @NonNull SugarAcidityZoneGeometryProfile geometryProfile;

  public ZoneGeometrySugarAcidityTargetResolver(
      @NonNull SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    this.geometryProfile = requireNonNull(geometryProfile, "sugar-acidity zone geometry profile cannot be null");
  }

  @Override
  public @NonNull PredefinedSugarAcidityZoneTarget resolvePredefinedZone(
      @NonNull SugarAcidityZone zone
  ) {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    return geometryProfile.find(zone)
        .map(this::toTarget)
        .orElseThrow(() -> new SugarAcidityTargetResolutionException(
            "No sugar-acidity zone geometry found for " + zone
        ));
  }

  private @NonNull PredefinedSugarAcidityZoneTarget toTarget(
      @NonNull SugarAcidityZoneGeometry zone
  ) {
    if (zone instanceof RectangleSugarAcidityZoneGeometry rectangle) {
      return new PredefinedSugarAcidityZoneTarget(
          zone.zone(),
          RectangleSugarAcidityTargetGeometry.from(rectangle)
      );
    }
    // TODO Support ellipse targets when blend distance objective functions handle non-rectangle geometry.
    throw new SugarAcidityTargetResolutionException(
        "Unsupported sugar-acidity target geometry: " + zone.shape()
    );
  }
}
