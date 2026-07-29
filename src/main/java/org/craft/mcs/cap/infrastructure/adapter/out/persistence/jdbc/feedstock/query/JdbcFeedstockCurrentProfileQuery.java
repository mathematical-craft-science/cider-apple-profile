package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialStateView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetMetadataView;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetMetadataUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads state-aware composite current profiles for Feedstock batches. */
@Component
@RequiredArgsConstructor
class JdbcFeedstockCurrentProfileQuery
    implements FeedstockCurrentProfileQueryRepository, FeedstockCurrentProfileAnalysisCandidateRepository {

  private static final Set<MeasurementCharacteristic> PROFILE_CHARACTERISTICS = Set.copyOf(
      EnumSet.of(
          MeasurementCharacteristic.SPECIFIC_GRAVITY,
          MeasurementCharacteristic.TITRATABLE_ACIDITY,
          MeasurementCharacteristic.PH,
          MeasurementCharacteristic.TANNIN,
          MeasurementCharacteristic.YAN
      )
  );

  private static final Comparator<FeedstockCurrentProfileView> CANDIDATE_ORDER =
      Comparator.comparing(
              (FeedstockCurrentProfileView view) -> view.latestMeasurementSet().measuredAt(),
              Comparator.reverseOrder()
          )
          .thenComparing(
              view -> view.latestMeasurementSet().measurementSetId().toString(),
              Comparator.reverseOrder()
          );

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull ProjectCurrentMaterialStateUseCase currentStateProjector;
  private final @NonNull QueryMeasurementSetMetadataUseCase measurementSetMetadataQuery;

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findCandidates(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch analysis candidate query cannot be null");
    return loadViews(queryEligibleBatchIds(query)).stream()
        .sorted(CANDIDATE_ORDER)
        .filter(view -> isAfter(view, query.after()))
        .limit(query.limit())
        .toList();
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockCurrentProfileView> findByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return loadViews(List.of(batchId)).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    return loadViews(List.copyOf(batchIds));
  }

  private @NonNull List<@NonNull FeedstockBatchId> queryEligibleBatchIds(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  ) {
    final var sql = new StringBuilder("""
        SELECT batch.batch_id
          FROM feedstock_batches batch
         WHERE batch.workspace_id = :workspaceId
           AND batch.deleted_at IS NULL
        """);
    final var parameters = new MapSqlParameterSource("workspaceId", query.workspaceId().value());
    if (query.batch() != null) {
      sql.append(" AND batch.batch_id IN (:batchIds)");
      parameters.addValue(
          "batchIds",
          query.batch().batchIds().stream().map(FeedstockBatchId::value).toList() // NOSONAR
      );
    }
    if (query.season() != null) {
      sql.append("""
           AND batch.season_starts_on <= :seasonEndsOn
           AND batch.season_ends_on >= :seasonStartsOn
          """);
      parameters
          .addValue("seasonStartsOn", query.season().startsOn()) // NOSONAR
          .addValue("seasonEndsOn", query.season().endsOn()); // NOSONAR
    }
    sql.append(" ORDER BY batch.batch_id");

    // SQL structure is composed only from internal constant fragments; values remain bound parameters.
    //noinspection SqlSourceToSinkFlow
    return jdbcTemplate.query( // NOSONAR
        sql.toString(),
        parameters,
        (resultSet, rowNumber) -> FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class))
    );
  }

  private static boolean isAfter(
      @NonNull FeedstockCurrentProfileView view,
      @Nullable FeedstockAnalysisCandidatePagePosition position
  ) {
    if (position == null) {
      return true;
    }
    final var latestMeasurementSet = view.latestMeasurementSet();
    final var timeComparison = latestMeasurementSet.measuredAt().compareTo(position.measuredAt());
    return timeComparison < 0
        || timeComparison == 0
        && latestMeasurementSet.measurementSetId().toString().compareTo(position.measurementId().toString()) < 0;
  }

  private @NonNull List<@NonNull FeedstockCurrentProfileView> loadViews(
      @NonNull List<@NonNull FeedstockBatchId> batchIds
  ) {
    if (batchIds.isEmpty()) {
      return List.of();
    }
    final var workspacesByBatch = loadWorkspaces(batchIds);
    final Map<FeedstockBatchId, CurrentMaterialStateView> statesByBatch = new LinkedHashMap<>();
    workspacesByBatch.entrySet().stream()
        .collect(Collectors.groupingBy(Map.Entry::getValue))
        .forEach((workspaceId, batchEntries) -> {
          final var subjects = batchEntries.stream()
              .map(Map.Entry::getKey)
              .map(JdbcFeedstockCurrentProfileQuery::subject)
              .collect(Collectors.toUnmodifiableSet());
          currentStateProjector.projectAll(workspaceId, subjects)
              .forEach((subject, state) -> statesByBatch.put(
                  FeedstockBatchId.of(subject.id()),
                  state
              ));
        });

    final var measurementSetIds = statesByBatch.values().stream()
        .flatMap(state -> state.characteristics().stream())
        .filter(characteristic -> PROFILE_CHARACTERISTICS.contains(characteristic.characteristic()))
        .map(CurrentMaterialCharacteristicView::evidence)
        .map(MeasurementEvidenceView::evidenceReference)
        .map(MeasurementEvidenceReference::measurementSetId)
        .map(MeasurementSetId::of)
        .collect(Collectors.toUnmodifiableSet());
    final var metadataBySet = workspacesByBatch.values().stream()
        .distinct()
        .flatMap(workspaceId -> measurementSetMetadataQuery.findAll(workspaceId, measurementSetIds)
            .entrySet()
            .stream())
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    return batchIds.stream()
        .map(batchId -> toView(batchId, statesByBatch.get(batchId), metadataBySet))
        .flatMap(Optional::stream)
        .toList();
  }

  private @NonNull Map<@NonNull FeedstockBatchId, @NonNull WorkspaceId> loadWorkspaces(
      @NonNull List<@NonNull FeedstockBatchId> batchIds
  ) {
    return jdbcTemplate.query(
            """
                SELECT batch_id,
                       workspace_id
                  FROM feedstock_batches
                 WHERE batch_id IN (:batchIds)
                   AND deleted_at IS NULL
                """,
            new MapSqlParameterSource(
                "batchIds",
                batchIds.stream().map(FeedstockBatchId::value).toList()
            ),
            (resultSet, rowNumber) -> Map.entry(
                FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
                WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class))
            )
        )
        .stream()
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static @NonNull Optional<@NonNull FeedstockCurrentProfileView> toView(
      @NonNull FeedstockBatchId batchId,
      @Nullable CurrentMaterialStateView state,
      @NonNull Map<@NonNull MeasurementSetId, @NonNull MeasurementSetMetadataView> metadataBySet
  ) {
    if (state == null || state.characteristics().isEmpty()) {
      return Optional.empty();
    }
    final var evidence = state.characteristics().stream()
        .filter(characteristic -> PROFILE_CHARACTERISTICS.contains(characteristic.characteristic()))
        .map(CurrentMaterialCharacteristicView::evidence)
        .toList();
    if (evidence.isEmpty()) {
      return Optional.empty();
    }
    final var anchor = evidence.stream()
        .max(Comparator
            .comparing(MeasurementEvidenceView::measuredAt)
            .thenComparing(item -> item.evidenceReference().recordId())
            .thenComparing(item -> item.evidenceReference().measurementSetId()))
        .orElseThrow();
    final var anchorReference = anchor.evidenceReference();
    final var metadata = requireNonNull(
        metadataBySet.get(MeasurementSetId.of(anchorReference.measurementSetId())),
        "measurement set metadata cannot be null"
    );
    return Optional.of(new FeedstockCurrentProfileView(
        batchId,
        state.currentMaterialStateId(),
        state.value(MeasurementCharacteristic.SPECIFIC_GRAVITY, SpecificGravityValue.class).orElse(null), // NOSONAR
        state.value(MeasurementCharacteristic.TITRATABLE_ACIDITY, TitratableAcidityValue.class)
            .orElse(null), // NOSONAR
        state.value(MeasurementCharacteristic.PH, PhValue.class).orElse(null), // NOSONAR
        state.value(MeasurementCharacteristic.TANNIN, TanninValue.class).orElse(null), // NOSONAR
        state.value(MeasurementCharacteristic.YAN, YeastAssimilableNitrogenValue.class).orElse(null), // NOSONAR
        evidence,
        new FeedstockLatestMeasurementSetView(
            anchorReference.measurementSetId(),
            metadata.name().value(),
            anchor.measuredAt(),
            metadata.version(),
            metadata.note()
        )
    ));
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull FeedstockBatchId batchId) {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, batchId.value());
  }

}
