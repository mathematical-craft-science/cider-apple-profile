package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.FeedstockMeasurementViews.view;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.AssessFeedstockMeasurementSugarAcidityZoneService;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;


class AssessFeedstockMeasurementSugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new AssessFeedstockMeasurementSugarAcidityZoneService(
        repository,
        batchRepository,
        mock(FeedstockAccessControl.class),
        projector()
    );
    final var sample = FeedstockMeasurementFixture.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Partial batch"),
        sugarOnlyProfile(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(view(sample)));
    when(batchRepository.findById(sample.batchId()))
        .thenReturn(Optional.of(batch(sample.batchId(), WorkspaceId.of(newUuid()))));

    final var result = service.assess(sample.measurementId());

    assertThat(result.evaluation())
        .returns(false, ChemicalProfileAssessment::plottable)
        .returns(null, ChemicalProfileAssessment::assessment)
        .extracting(ChemicalProfileAssessment::capabilityAssessment)
        .returns(
            ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH,
            ChemicalProfileCapabilityAssessment::capability
        ).satisfies(assessment ->
            assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.TITRATABLE_ACIDITY)
        );
  }

  @Test
  void reportsMissingBatchWithApplicationNotFoundException() {
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var service = new AssessFeedstockMeasurementSugarAcidityZoneService(
        repository,
        batchRepository,
        mock(FeedstockAccessControl.class),
        projector()
    );
    final var sample = FeedstockMeasurementFixture.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Orphan measurement"),
        sugarOnlyProfile(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
    when(repository.findById(sample.measurementId())).thenReturn(Optional.of(view(sample)));
    when(batchRepository.findById(sample.batchId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assess(sample.measurementId()))
        .isInstanceOf(FeedstockBatchNotFoundException.class)
        .hasMessageContaining(sample.batchId().value().toString());
  }

  private static @NonNull ChemicalProfile sugarOnlyProfile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();
  }

  private static @NonNull ChemicalProfileAssessmentAdapter projector() {
    return chemicalProfileAssessmentAdapter();
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull FeedstockBatchId batchId,
      @NonNull WorkspaceId workspaceId
  ) {
    return FeedstockBatch.create(
        batchId,
        workspaceId,
        BatchName.of("Test batch"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("100")
    );
  }
}
