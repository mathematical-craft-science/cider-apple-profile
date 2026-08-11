package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageClassification;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsQuery;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselCurrentContentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselResponse;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class VesselRestMapperTest {

  @Test
  void mapsCurrentMaterialUsageToVesselResponse() {
    final var vesselId = VesselId.newId();
    final var content = new VesselCurrentContentView(
        vesselId,
        VesselCurrentContentType.FEEDSTOCK_BATCH,
        newUuid(),
        MaterialStateId.newId(),
        BatchName.of("October juice"),
        VolumeValue.ofLiters("125")
    );
    final var response = VesselRestMapper.toResponse(new VesselUsageView(
        vesselView(vesselId),
        content
    ));

    assertThat(response)
        .returns(VesselUsageClassification.PARTIALLY_FILLED, VesselResponse::usage)
        .satisfies(result -> assertThat(result.usedVolume())
            .returns("L", DecimalQuantityResponse::unit)
            .satisfies(quantity -> assertThat(quantity.value()).isEqualByComparingTo("125")))
        .satisfies(result -> assertThat(result.remainingCapacity().value()).isEqualByComparingTo("375"))
        .satisfies(result -> assertThat(result.currentContent())
            .isNotNull()
            .returns(VesselCurrentContentType.FEEDSTOCK_BATCH, VesselCurrentContentResponse::subjectType)
            .returns(content.subjectId(), VesselCurrentContentResponse::subjectId)
            .returns(content.materialStateId().value(), VesselCurrentContentResponse::materialStateId)
            .returns("October juice", VesselCurrentContentResponse::displayName));
  }

  @Test
  void mapsEmptyVesselWithoutCurrentContent() {
    final var response = VesselRestMapper.toResponse(VesselUsageView.empty(vesselView(VesselId.newId())));

    assertThat(response)
        .returns(null, VesselResponse::currentContent)
        .returns(VesselUsageClassification.EMPTY, VesselResponse::usage)
        .satisfies(result -> assertThat(result.usedVolume().value()).isZero())
        .satisfies(result -> assertThat(result.remainingCapacity().value()).isEqualByComparingTo("500"));
  }

  @Test
  void mapsAvailableVesselSearchCriteria() {
    final var workspaceId = WorkspaceId.newId();

    assertThat(VesselRestMapper.toAvailableQuery(
        workspaceId.value(),
        "  tank f  ",
        new BigDecimal("250.000")
    ))
        .returns(workspaceId, ListAvailableVesselsQuery::workspaceId)
        .returns(VolumeValue.ofLiters("250.000"), ListAvailableVesselsQuery::requiredVolume)
        .satisfies(query -> assertThat(query.nameContains())
            .isNotNull()
            .extracting(TextSearchCriteria::query)
            .isEqualTo("tank f"));
    assertThat(VesselRestMapper.toAvailableQuery(workspaceId.value(), "  ", null))
        .returns(null, ListAvailableVesselsQuery::nameContains)
        .returns(null, ListAvailableVesselsQuery::requiredVolume);
  }

  private static VesselView vesselView(VesselId vesselId) {
    return new VesselView(
        vesselId,
        WorkspaceId.newId(),
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL,
        VesselOperationalStatus.OPERATIONAL,
        AggregateRevision.of(2)
    );
  }
}
