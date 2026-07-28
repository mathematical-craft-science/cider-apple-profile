package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.EllipseSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.RectangleSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Resolves predefined targets from configured SG/TA zone geometries.
 */
public final class ZoneGeometrySugarAcidityTargetResolver
    implements SugarAcidityTargetResolver {

  private final @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile;

  public ZoneGeometrySugarAcidityTargetResolver(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    this.geometryProfile = requireNonNull(
        geometryProfile,
        "sugar-acidity zone geometry profile cannot be null"
    );
  }

  @Override
  public @NonNull PredefinedJolicoeurSugarAcidityZoneTarget resolvePredefinedZone(
      @NonNull JolicoeurSugarAcidityZone zone
  ) {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    return geometryProfile.find(zone)
        .map(this::toTarget)
        .orElseThrow(() -> new SugarAcidityTargetResolutionException(
            "No sugar-acidity zone geometry found for " + zone
        ));
  }

  private @NonNull PredefinedJolicoeurSugarAcidityZoneTarget toTarget(
      @NonNull JolicoeurSugarAcidityZoneGeometry geometry
  ) {
    return switch (geometry) {
      case RectangleSugarAcidityZoneGeometry(
          var zone,
          var rectangle
      ) -> new PredefinedJolicoeurSugarAcidityZoneTarget(
          zone,
          new RectangleSugarAcidityTargetGeometry(rectangle)
      );
      case EllipseSugarAcidityZoneGeometry(
          var zone,
          var ellipse
      ) -> new PredefinedJolicoeurSugarAcidityZoneTarget(
          zone,
          new EllipseSugarAcidityTargetGeometry(ellipse)
      );
    };
  }
}
