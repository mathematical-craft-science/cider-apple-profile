package org.craft.mcs.cap.core.measurement.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSet;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;


/** Append-oriented aggregate repository for measurement sets and immutable revisions. */
public interface MeasurementSetRepository {

  @NonNull VersionedAggregate<MeasurementSet> create(@NonNull MeasurementSet measurementSet);

  @NonNull VersionedAggregate<MeasurementSet> appendRevision(
      @NonNull VersionedAggregate<MeasurementSet> measurementSet
  );

  @NonNull VersionedAggregate<MeasurementSet> retract(
      @NonNull VersionedAggregate<MeasurementSet> measurementSet
  );

  @NonNull Optional<@NonNull VersionedAggregate<MeasurementSet>> findVersionedById(
      @NonNull MeasurementSetId measurementSetId
  );

}
