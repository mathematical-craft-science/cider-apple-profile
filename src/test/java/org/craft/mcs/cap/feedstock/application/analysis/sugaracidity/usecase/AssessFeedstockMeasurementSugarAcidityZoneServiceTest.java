package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.*;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AssessFeedstockMeasurementSugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(FeedstockMeasurementRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new AssessFeedstockMeasurementSugarAcidityZoneService(
        repository,
        batchRepository,
        mock(FeedstockAccessControl.class),
        projector()
    );
    final var sample = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Partial batch"),
        sugarOnlyProfile(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), WorkspaceId.of(newUuid()))));

    final var result = service.assess(sample.measurementId());

    assertThat(result.evaluation())
        .returns(false, SugarAcidityProfileEvaluation::plottable)
        .returns(null, SugarAcidityProfileEvaluation::assessment)
        .extracting(SugarAcidityProfileEvaluation::capabilityAssessment)
        .returns(
            ChemicalProfileCapability.SUGAR_ACIDITY_PLOT,
            ChemicalProfileCapabilityAssessment::capability
        ).satisfies(assessment ->
            assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.ACIDITY)
        );
  }

  @Test
  void reportsMissingBatchWithApplicationNotFoundException() {
    final var repository = mock(FeedstockMeasurementRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new AssessFeedstockMeasurementSugarAcidityZoneService(
        repository,
        batchRepository,
        mock(FeedstockAccessControl.class),
        projector()
    );
    final var sample = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Orphan measurement"),
        sugarOnlyProfile(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(sample));
    when(batchRepository.findById(sample.batchId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assess(sample.measurementId()))
        .isInstanceOf(FeedstockBatchNotFoundException.class)
        .hasMessageContaining(sample.batchId().value().toString());
  }

  private static @NonNull ChemicalProfile sugarOnlyProfile() {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
  }

  private static @NonNull SugarAcidityProfileProjector projector() {
    return new SugarAcidityProfileProjector(
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
    );
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull FeedstockBatchId batchId,
      @NonNull WorkspaceId workspaceId
  ) {
    return FeedstockBatch.create(
        batchId,
        workspaceId,
        FeedstockBatchName.of("Test batch"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
