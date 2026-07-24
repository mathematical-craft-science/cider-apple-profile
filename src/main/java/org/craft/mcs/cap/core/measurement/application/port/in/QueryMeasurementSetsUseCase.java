package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.jspecify.annotations.NonNull;


public interface QueryMeasurementSetsUseCase {

  @NonNull MeasurementSetView get(@NonNull MeasurementSetId measurementSetId);

  @NonNull List<@NonNull MeasurementSetView> list(@NonNull MeasurementSubjectReference subject);
}
