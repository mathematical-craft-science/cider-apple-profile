package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.EllipseSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.RectangleSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneId;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometryProfile;
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
      @NonNull JolicoeurSugarAcidityZoneId zone
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
