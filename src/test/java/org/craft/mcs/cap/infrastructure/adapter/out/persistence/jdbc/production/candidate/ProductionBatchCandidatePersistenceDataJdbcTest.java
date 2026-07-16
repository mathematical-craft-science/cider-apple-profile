package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileDistanceMetric;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    ProductionBatchCandidateJsonCodec.class,
    JdbcProductionBatchCandidateStore.class
})
class ProductionBatchCandidatePersistenceDataJdbcTest {

  @Autowired
  private JdbcProductionBatchCandidateStore store;

  @Autowired
  private SpringDataProductionBatchCandidateJdbcRepository springDataRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void roundTripsCandidateAnalysisAndUpdatesConfirmationState() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.save(candidate(workspaceId, sourceReference));

    final var restored = store.findById(saved.candidate().candidateId());

    assertThat(saved.revision())
        .isPositive();
    assertThat(restored)
        .get()
        .satisfies(snapshot -> assertThat(snapshot.candidate())
            .returns(ProductionBatchCandidateStatus.AVAILABLE, ProductionBatchCandidate::status)
            .returns(productionProfile(), ProductionBatchCandidate::profile)
            .satisfies(candidate -> assertThat(candidate.evidenceFor(sourceReference))
                .isEqualTo(ProductionSourceEvidenceReference.of("measurement-2026-07-16"))))
        .satisfies(snapshot -> assertThat(snapshot)
            .returns(ProfileAxisNormalizerCode.SENSITIVITY_UNIT, ProductionBatchCandidateSnapshot::normalizerCode)
            .returns(List.of(ChemicalProfileCompositionWarning.TANNIN_INCOMPLETE),
                ProductionBatchCandidateSnapshot::warnings)
            .satisfies(candidateSnapshot -> assertThat(candidateSnapshot.targetDistance())
                .isNotNull()
                .returns(new BigDecimal("0.15"), ProfileTargetDistance::value)));

    final var productionBatchId = persistProductionBatch(workspaceId);
    final var candidateToConfirm = restored.orElseThrow().candidate();
    candidateToConfirm.confirm(productionBatchId);
    final var confirmed = store.save(restored.orElseThrow().withCandidate(candidateToConfirm));

    assertThat(springDataRepository.count())
        .isOne();
    assertThat(confirmed.candidate())
        .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidate::status)
        .returns(productionBatchId, ProductionBatchCandidate::confirmedProductionBatchId);
    assertThat(confirmed.revision())
        .isGreaterThan(saved.revision());
  }

  @Test
  void rejectsConfirmationFromStaleCandidateRevision() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.save(candidate(workspaceId, sourceReference));
    final var firstCopy = store.findById(saved.candidate().candidateId()).orElseThrow();
    final var staleCopy = store.findById(saved.candidate().candidateId()).orElseThrow();
    final var firstProductionBatchId = persistProductionBatch(workspaceId);
    firstCopy.candidate().confirm(firstProductionBatchId);
    store.save(firstCopy.withCandidate(firstCopy.candidate()));

    staleCopy.candidate().confirm(persistProductionBatch(workspaceId));

    assertThatThrownBy(() -> store.save(staleCopy.withCandidate(staleCopy.candidate())))
        .isInstanceOf(ConcurrentResourceModificationException.class);
    assertThat(store.findById(saved.candidate().candidateId()))
        .get()
        .extracting(ProductionBatchCandidateSnapshot::candidate)
        .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidate::status)
        .returns(firstProductionBatchId, ProductionBatchCandidate::confirmedProductionBatchId);
  }

  private @NonNull ProductionBatchCandidateSnapshot candidate(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var composition = ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
        sourceReference,
        VolumeValue.ofLiters("120")
    )));
    return new ProductionBatchCandidateSnapshot(
        ProductionBatchCandidate.create(
            ProductionBatchCandidateId.newId(),
            workspaceId,
            composition,
            Map.of(sourceReference, ProductionSourceEvidenceReference.of("measurement-2026-07-16")),
            productionProfile()
        ),
        ProfileAxisNormalizerCode.SENSITIVITY_UNIT,
        targetDistance(),
        List.of(ChemicalProfileCompositionWarning.TANNIN_INCOMPLETE)
    );
  }

  private static @NonNull ProductionProfileSnapshot productionProfile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        TanninityValue.of("1200"),
        YeastAssimilableNitrogenValue.of("180")
    );
  }

  private static @NonNull ProfileTargetDistance targetDistance() {
    final var point = new SugarAcidityCoordinates(new BigDecimal("1.052"), new BigDecimal("6.2"));
    return new ProfileTargetDistance(
        ProfileTargetDistanceStatus.SCORED,
        point,
        new SugarAcidityCoordinates(new BigDecimal("1.050"), new BigDecimal("6.0")),
        new BigDecimal("1200"),
        new BigDecimal("1200"),
        new BigDecimal("0.15"),
        false,
        false,
        ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA,
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_MANHATTAN,
        ProfileTargetObjectiveWeights.balanced(),
        SugarAcidityTargetKind.PREDEFINED_ZONE,
        SugarAcidityTargetGeometryShape.RECTANGLE,
        List.of()
    );
  }

  private @NonNull WorkspaceId persistWorkspace() {
    final var workspaceId = WorkspaceId.newId();
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Candidate workspace', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    return workspaceId;
  }

  private @NonNull ProductionBatchId persistProductionBatch(@NonNull WorkspaceId workspaceId) {
    final var productionBatchId = ProductionBatchId.newId();
    jdbcTemplate.update(
        """
            INSERT INTO production_batches (
                batch_id, workspace_id, name, kind, status, total_volume_liters,
                sugar_specific_gravity, acidity_g_l_as_malic, tannin_mg_l_tae, yan_mg_n_l,
                version, created_at, updated_at
            ) VALUES (?, ?, 'Confirmed batch', 'CIDER', 'PLANNED', 120, 1.052, 6.2, 1200, 180,
                      0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        productionBatchId.value(),
        workspaceId.value()
    );
    return productionBatchId;
  }
}
