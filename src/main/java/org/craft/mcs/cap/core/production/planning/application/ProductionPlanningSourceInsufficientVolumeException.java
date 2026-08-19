package org.craft.mcs.cap.core.production.planning.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Indicates that a source cannot provide the volume requested by a planning candidate. */
@SuppressWarnings("java:S110")
public final class ProductionPlanningSourceInsufficientVolumeException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;
  private final transient @NonNull VolumeValue requestedVolume;
  private final transient @NonNull VolumeValue availableVolume;

  public ProductionPlanningSourceInsufficientVolumeException(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull VolumeValue requestedVolume,
      @NonNull VolumeValue availableVolume
  ) {
    super("not enough available volume in production source: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
    this.requestedVolume = requireNonNull(requestedVolume, "requested volume cannot be null");
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }

  public @NonNull VolumeValue requestedVolume() {
    return requestedVolume;
  }

  public @NonNull VolumeValue availableVolume() {
    return availableVolume;
  }
}
