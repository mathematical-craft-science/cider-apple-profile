package org.craft.mcs.cap.core.cideranalysis.graph.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphMetadataView;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.graph.application.port.in.QueryJolicoeurSugarAcidityGraphMetadataUseCase;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.EllipseSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.RectangleSugarAcidityZoneGeometry;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Publishes configured graph geometry without exposing CiderAnalysis domain objects. */
@Component
@RequiredArgsConstructor
public final class JolicoeurSugarAcidityGraphMetadataService
    implements QueryJolicoeurSugarAcidityGraphMetadataUseCase {

  private final @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile;

  @Override
  public @NonNull JolicoeurSugarAcidityGraphMetadataView query() {
    final var specificGravity = geometryProfile.specificGravityScale();
    final var titratableAcidity = geometryProfile.titratableAcidityScale();
    return new JolicoeurSugarAcidityGraphMetadataView(
        new JolicoeurSugarAcidityGraphMetadataView.SpecificGravityAxis(
            specificGravity.min(),
            specificGravity.max(),
            specificGravity.boundaryValues()
        ),
        new JolicoeurSugarAcidityGraphMetadataView.TitratableAcidityAxis(
            titratableAcidity.min(),
            titratableAcidity.max(),
            titratableAcidity.boundaryValues()
        ),
        geometryProfile.zones().stream()
            .map(JolicoeurSugarAcidityGraphMetadataService::toView)
            .toList()
    );
  }

  private static JolicoeurSugarAcidityGraphMetadataView.@NonNull ZoneGeometry toView(
      @NonNull JolicoeurSugarAcidityZoneGeometry geometry
  ) {
    return switch (geometry) {
      case RectangleSugarAcidityZoneGeometry(var zone, var rectangle) ->
          new JolicoeurSugarAcidityGraphMetadataView.RectangleZone(
              JolicoeurSugarAcidityZoneCode.valueOf(zone.name()),
              rectangle.specificGravityRange(),
              rectangle.titratableAcidityRange()
          );
      case EllipseSugarAcidityZoneGeometry(var zone, var ellipse) ->
          new JolicoeurSugarAcidityGraphMetadataView.EllipseZone(
              JolicoeurSugarAcidityZoneCode.valueOf(zone.name()),
              ellipse.specificGravityCenter(),
              ellipse.titratableAcidityCenter(),
              ellipse.specificGravityRadius(),
              ellipse.titratableAcidityRadius()
          );
    };
  }
}
