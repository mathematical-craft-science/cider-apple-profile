package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.planning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateNormalizer;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateWarning;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionCandidateSourceEvidence;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.productionCandidatePrediction;

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
    final var saved = store.create(candidate(workspaceId, sourceReference));
    final var sourcesJson = jdbcTemplate.queryForObject(
        "SELECT sources_json FROM production_batch_candidates WHERE candidate_id = ?",
        String.class,
        saved.candidate().candidateId().value()
    );

    final var restored = store.findById(saved.candidate().candidateId());

    assertThat(sourcesJson)
        .contains("\"type\":\"FEEDSTOCK_BATCH\"")
        .contains("\"volumeLiters\":120")
        .doesNotContain("org.craft.mcs.cap");
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
            .returns(ProductionCandidateNormalizer.SENSITIVITY_UNIT, ProductionBatchCandidateSnapshot::normalizerCode)
            .satisfies(candidateSnapshot -> assertThat(candidateSnapshot.potentialAbv().potentialAbv().percent())
                .isEqualByComparingTo("6.825"))
            .returns(List.of(ProductionCandidateWarning.TANNIN_INCOMPLETE),
                ProductionBatchCandidateSnapshot::warnings)
            .satisfies(candidateSnapshot -> assertThat(candidateSnapshot.targetDistance())
                .isNotNull()
                .returns(new BigDecimal("0.15"), ProductionCandidateTargetDistanceView::value)
                .returns(85, ProductionCandidateTargetDistanceView::matchPercent)
                .returns(
                    ProductionCandidateTargetDistanceView.FitStatus.CLOSE,
                    ProductionCandidateTargetDistanceView::fitStatus
                )));

    final var productionBatchId = persistProductionBatch(workspaceId);
    final var candidateToConfirm = restored.orElseThrow().candidate();
    candidateToConfirm.confirm(productionBatchId);
    final var confirmed = store.update(restored.orElseThrow().withCandidate(candidateToConfirm));

    assertThat(springDataRepository.count())
        .isOne();
    assertThat(confirmed.candidate())
        .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidate::status)
        .returns(productionBatchId, ProductionBatchCandidate::confirmedProductionBatchId);
    assertThat(confirmed.revision())
        .isGreaterThan(saved.revision());
  }

  @Test
  void restoresTargetFitForLegacyCandidateJson() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    jdbcTemplate.update(
        """
            UPDATE production_batch_candidates
            SET target_distance_json = (
                target_distance_json::jsonb - 'matchPercent' - 'fitStatus'
            )::text
            WHERE candidate_id = ?
            """,
        saved.candidate().candidateId().value()
    );

    assertThat(store.findById(saved.candidate().candidateId()))
        .get()
        .extracting(ProductionBatchCandidateSnapshot::targetDistance)
        .isNotNull()
        .returns(87, ProductionCandidateTargetDistanceView::matchPercent)
        .returns(ProductionCandidateTargetDistanceView.FitStatus.CLOSE,
            ProductionCandidateTargetDistanceView::fitStatus);
  }

  @Test
  void rejectsIncompletePersistedTargetFit() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    jdbcTemplate.update(
        """
            UPDATE production_batch_candidates
            SET target_distance_json = (
                target_distance_json::jsonb - 'fitStatus'
            )::text
            WHERE candidate_id = ?
            """,
        saved.candidate().candidateId().value()
    );

    assertThatThrownBy(() -> store.findById(saved.candidate().candidateId()))
        .isInstanceOf(DataRetrievalFailureException.class)
        .hasMessage("persisted production target fit is incomplete");
  }

  @Test
  void translatesSemanticallyInvalidPersistedTargetDistance() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    jdbcTemplate.update(
        """
            UPDATE production_batch_candidates
            SET target_distance_json = jsonb_set(
                target_distance_json::jsonb,
                '{matchPercent}',
                '101'::jsonb
            )::text
            WHERE candidate_id = ?
            """,
        saved.candidate().candidateId().value()
    );

    assertThatThrownBy(() -> store.findById(saved.candidate().candidateId()))
        .isInstanceOf(DataRetrievalFailureException.class)
        .hasMessage("persisted production target distance is invalid")
        .hasRootCauseMessage("production candidate target match percent must be between 0 and 100");
  }

  @Test
  void rejectsPersistedTargetPointForAnotherCalculatedProfile() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    jdbcTemplate.update(
        """
            UPDATE production_batch_candidates
            SET target_distance_json = jsonb_set(
                target_distance_json::jsonb,
                '{point,specificGravity}',
                '1.061'::jsonb
            )::text
            WHERE candidate_id = ?
            """,
        saved.candidate().candidateId().value()
    );

    assertThatThrownBy(() -> store.findById(saved.candidate().candidateId()))
        .isInstanceOf(DataRetrievalFailureException.class)
        .hasMessage("persisted production batch candidate is invalid")
        .hasRootCauseMessage("production candidate target point must match the calculated profile");
  }

  @Test
  void rejectsPersistedTargetTanninForAnotherCalculatedProfile() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    jdbcTemplate.update(
        """
            UPDATE production_batch_candidates
            SET target_distance_json = jsonb_set(
                target_distance_json::jsonb,
                '{tannin}',
                '1300'::jsonb
            )::text
            WHERE candidate_id = ?
            """,
        saved.candidate().candidateId().value()
    );

    assertThatThrownBy(() -> store.findById(saved.candidate().candidateId()))
        .isInstanceOf(DataRetrievalFailureException.class)
        .hasMessage("persisted production batch candidate is invalid")
        .hasRootCauseMessage("production candidate target tannin must match the calculated profile");
  }

  @Test
  void rejectsPersistedTotalVolumeThatDoesNotMatchSources() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    jdbcTemplate.update(
        """
            UPDATE production_batch_candidates
            SET total_volume_liters = 121
            WHERE candidate_id = ?
            """,
        saved.candidate().candidateId().value()
    );

    assertThatThrownBy(() -> store.findById(saved.candidate().candidateId()))
        .isInstanceOf(DataRetrievalFailureException.class)
        .hasMessage("persisted production candidate total volume does not match its sources");
  }

  @Test
  void roundTripsAuthoritativeUnscorableTargetFit() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference, unscorableTargetDistance()));

    assertThat(store.findById(saved.candidate().candidateId()))
        .get()
        .extracting(ProductionBatchCandidateSnapshot::targetDistance)
        .isNotNull()
        .returns(null, ProductionCandidateTargetDistanceView::matchPercent)
        .returns(
            ProductionCandidateTargetDistanceView.FitStatus.UNSCORABLE,
            ProductionCandidateTargetDistanceView::fitStatus
        );
  }

  @Test
  void rejectsConfirmationFromStaleCandidateRevision() {
    final var workspaceId = persistWorkspace();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var saved = store.create(candidate(workspaceId, sourceReference));
    final var firstCopy = store.findById(saved.candidate().candidateId()).orElseThrow();
    final var staleCopy = store.findById(saved.candidate().candidateId()).orElseThrow();
    final var firstProductionBatchId = persistProductionBatch(workspaceId);
    firstCopy.candidate().confirm(firstProductionBatchId);
    store.update(firstCopy.withCandidate(firstCopy.candidate()));

    staleCopy.candidate().confirm(persistProductionBatch(workspaceId));

    assertThatThrownBy(() -> store.update(staleCopy.withCandidate(staleCopy.candidate())))
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
    return candidate(workspaceId, sourceReference, targetDistance());
  }

  private @NonNull ProductionBatchCandidateSnapshot candidate(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceReference sourceReference,
      @NonNull ProductionCandidateTargetDistanceView targetDistance
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
            Map.of(
                sourceReference,
                new ProductionCandidateSourceEvidence(
                    ProductionSourceEvidenceReference.of("measurement-2026-07-16"),
                    MaterialStateId.newId()
                )
            ),
            productionProfile()
        ),
        productionCandidatePrediction(productionProfile().specificGravity()),
        ProductionCandidateNormalizer.SENSITIVITY_UNIT,
        targetDistance,
        List.of(ProductionCandidateWarning.TANNIN_INCOMPLETE)
    );
  }

  private static @NonNull ProductionProfileSnapshot productionProfile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        TanninValue.of("1200"),
        YeastAssimilableNitrogenValue.of("180")
    );
  }

  private static @NonNull ProductionCandidateTargetDistanceView targetDistance() {
    final var point = new ProductionCandidateTargetDistanceView.Coordinates(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2")
    );
    return new ProductionCandidateTargetDistanceView(
        ProductionCandidateTargetDistanceView.Status.SCORED,
        point,
        new ProductionCandidateTargetDistanceView.Coordinates(
            SpecificGravityValue.of("1.050"),
            TitratableAcidityValue.of("6.0")
        ),
        new ProductionCandidateTargetDistanceView.TargetPosition(
            ProductionCandidateTargetDistanceView.AxisPosition.ABOVE,
            ProductionCandidateTargetDistanceView.AxisPosition.ABOVE
        ),
        TanninValue.of("1200"),
        TanninValue.of("1200"),
        new BigDecimal("0.15"),
        85,
        ProductionCandidateTargetDistanceView.FitStatus.CLOSE,
        false,
        false,
        ProductionBatchCandidateCalculationRequest.ObjectiveFunction.MANHATTAN_DISTANCE_TO_TARGET_AREA,
        ProductionCandidateTargetDistanceView.Metric.NORMALIZED_WEIGHTED_MANHATTAN,
        new ProductionCandidateTargetDistanceView.Weights(BigDecimal.ONE, BigDecimal.ONE, null),
        ProductionCandidateTargetDistanceView.TargetKind.PREDEFINED_ZONE,
        ProductionCandidateTargetDistanceView.GeometryShape.RECTANGLE,
        Set.of()
    );
  }

  private static @NonNull ProductionCandidateTargetDistanceView unscorableTargetDistance() {
    final var point = new ProductionCandidateTargetDistanceView.Coordinates(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2")
    );
    return new ProductionCandidateTargetDistanceView(
        ProductionCandidateTargetDistanceView.Status.UNSCORABLE,
        point,
        point,
        new ProductionCandidateTargetDistanceView.TargetPosition(
            ProductionCandidateTargetDistanceView.AxisPosition.WITHIN,
            ProductionCandidateTargetDistanceView.AxisPosition.WITHIN
        ),
        null,
        null,
        null,
        null,
        ProductionCandidateTargetDistanceView.FitStatus.UNSCORABLE,
        true,
        false,
        ProductionBatchCandidateCalculationRequest.ObjectiveFunction.MANHATTAN_DISTANCE_TO_TARGET_AREA,
        ProductionCandidateTargetDistanceView.Metric.NORMALIZED_WEIGHTED_MANHATTAN,
        new ProductionCandidateTargetDistanceView.Weights(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE),
        ProductionCandidateTargetDistanceView.TargetKind.PREDEFINED_ZONE,
        ProductionCandidateTargetDistanceView.GeometryShape.RECTANGLE,
        Set.of(ProductionCandidateTargetDistanceView.EvaluationAxis.TANNIN)
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
    final var vesselId = VesselId.newId();
    VesselTestFixtures.persist(
        jdbcTemplate,
        workspaceId,
        vesselId,
        "Candidate confirmation vessel " + vesselId.value()
    );
    jdbcTemplate.update(
        """
            INSERT INTO production_batches (
                batch_id, workspace_id, name, vessel_id, lifecycle, alcoholic_fermentation_state,
                current_volume_liters, current_material_state_id,
                initial_sugar_specific_gravity, initial_acidity_g_l_as_malic,
                initial_tannin_mg_l_tae, initial_yan_mg_n_l,
                version, created_at, updated_at
            ) VALUES (?, ?, 'Confirmed batch', ?, 'ACTIVE', 'NOT_STARTED',
                      120, ?, 1.052, 6.2, 1200, 180,
                      0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        productionBatchId.value(),
        workspaceId.value(),
        vesselId.value(),
        MaterialStateId.newId().value()
    );
    return productionBatchId;
  }
}
