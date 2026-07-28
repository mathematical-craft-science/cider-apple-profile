package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.jspecify.annotations.NonNull;


/**
 * Domain service interface for assigning a zone to one complete sugar-acidity graph point.
 *
 * <p>Implementations may use different geometries, for example rectangular Jolicoeur boundary
 * rules or ellipse membership based on graph metadata. Missing characteristics are handled before
 * this assessor receives a point.
 */
public interface JolicoeurSugarAcidityZoneAssessor {

  @NonNull JolicoeurSugarAcidityZoneAssessment assess(
      @NonNull SugarAcidityGraphPoint point
  );
}
