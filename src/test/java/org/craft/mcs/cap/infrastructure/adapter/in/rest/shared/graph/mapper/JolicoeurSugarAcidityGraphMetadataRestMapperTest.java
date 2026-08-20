package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.graph.application.usecase.JolicoeurSugarAcidityGraphMetadataService;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGeometryShape;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.EllipseSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.RectangleSugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAxisResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class JolicoeurSugarAcidityGraphMetadataRestMapperTest {

  @Test
  void mapsAxisBoundariesAndRectangularZoneGeometry() {
    final var response = JolicoeurSugarAcidityGraphMetadataRestMapper.toResponse(
        new JolicoeurSugarAcidityGraphMetadataService(
            BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        ).query()
    );

    assertThat(response)
        .satisfies(metadata -> assertThat(metadata.specificGravityAxis())
            .returns(CanonicalUnitLabels.SPECIFIC_GRAVITY, SugarAcidityGraphAxisResponse::unit)
            .satisfies(axis -> assertThat(axis.boundaryValues()).hasSize(2)))
        .satisfies(metadata -> assertThat(metadata.titratableAcidityAxis())
            .returns(CanonicalUnitLabels.TITRATABLE_ACIDITY, SugarAcidityGraphAxisResponse::unit)
            .satisfies(axis -> assertThat(axis.boundaryValues()).hasSize(2)))
        .satisfies(metadata -> assertThat(metadata.zones())
            .allSatisfy(zone -> assertThat(zone)
                .isInstanceOf(RectangleSugarAcidityGraphZoneResponse.class)
                .returns(SugarAcidityGeometryShape.RECTANGLE, SugarAcidityGraphZoneResponse::shape)));
  }

  @Test
  void mapsEllipticalZoneGeometryWithoutASecondDomainModel() {
    final var response = JolicoeurSugarAcidityGraphMetadataRestMapper.toResponse(
        new JolicoeurSugarAcidityGraphMetadataService(
            BuiltInJolicoeurSugarAcidityZoneProfiles.ellipticalZoneGeometryProfile()
        ).query()
    );

    assertThat(response.zones())
        .allSatisfy(zone -> assertThat(zone)
            .isInstanceOf(EllipseSugarAcidityGraphZoneResponse.class)
            .returns(SugarAcidityGeometryShape.ELLIPSE, SugarAcidityGraphZoneResponse::shape));
  }
}
