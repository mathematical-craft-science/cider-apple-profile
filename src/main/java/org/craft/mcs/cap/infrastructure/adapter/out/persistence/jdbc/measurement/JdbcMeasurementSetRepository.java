package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetAlreadyExistsException;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSetRepository;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/**
 * Measurement aggregate repository that keeps write and hydration mechanics in focused JDBC collaborators.
 */
@Repository
@RequiredArgsConstructor
class JdbcMeasurementSetRepository implements MeasurementSetRepository {

  private final @NonNull JdbcMeasurementSetAggregateWriter writer;
  private final @NonNull JdbcMeasurementSetAggregateReader reader;

  @Override
  public @NonNull VersionedAggregate<MeasurementSet> create(@NonNull MeasurementSet measurementSet) {
    requireNonNull(measurementSet, "measurement set cannot be null");
    try {
      return writer.create(measurementSet);
    } catch (DuplicateKeyException exception) {
      throw new MeasurementSetAlreadyExistsException(measurementSet.measurementSetId(), exception);
    }
  }

  @Override
  public @NonNull VersionedAggregate<MeasurementSet> appendRevision(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    return writer.appendRevision(requireNonNull(state, "versioned measurement set cannot be null"));
  }

  @Override
  public @NonNull VersionedAggregate<MeasurementSet> retract(
      @NonNull VersionedAggregate<MeasurementSet> state
  ) {
    return writer.retract(requireNonNull(state, "versioned measurement set cannot be null"));
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<MeasurementSet>> findVersionedById(
      @NonNull MeasurementSetId measurementSetId
  ) {
    return reader.findVersionedById(requireNonNull(
        measurementSetId,
        "measurement set id cannot be null"
    ));
  }
}
