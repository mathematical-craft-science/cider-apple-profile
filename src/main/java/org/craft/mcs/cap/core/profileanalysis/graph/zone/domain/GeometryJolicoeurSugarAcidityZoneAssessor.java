package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Assesses a representative graph point using an immutable zone geometry profile.
 *
 * <p>Rectangle and ellipse behavior is supplied by the geometry values themselves. The assessor
 * therefore has one zone-selection algorithm and does not duplicate geometry rules across strategy
 * classes.</p>
 */
public final class GeometryJolicoeurSugarAcidityZoneAssessor
    implements JolicoeurSugarAcidityZoneAssessor {

  private final @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile;

  public GeometryJolicoeurSugarAcidityZoneAssessor(
      @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile
  ) {
    this.geometryProfile = requireNonNull(
        geometryProfile,
        "sugar-acidity zone geometry profile cannot be null"
    );
  }

  @Override
  public @NonNull JolicoeurSugarAcidityZoneAssessment assess(
      @NonNull SugarAcidityGraphPoint point
  ) {
    requireNonNull(point, "sugar-acidity graph point cannot be null");
    return geometryProfile.zones()
        .stream()
        .filter(zone -> zone.contains(point))
        .findFirst()
        .map(JolicoeurSugarAcidityZoneGeometry::zone)
        .map(JolicoeurSugarAcidityZoneAssessment::classified)
        .orElseGet(JolicoeurSugarAcidityZoneAssessment::unclassified);
  }
}
