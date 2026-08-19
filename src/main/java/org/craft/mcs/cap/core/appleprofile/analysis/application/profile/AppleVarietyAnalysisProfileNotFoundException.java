package org.craft.mcs.cap.core.appleprofile.analysis.application.profile;

import java.io.Serial;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Indicates that no catalog reference profile can be resolved for apple-variety analysis. */
@SuppressWarnings("java:S110")
public final class AppleVarietyAnalysisProfileNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull AppleVarietyId varietyId;

  public AppleVarietyAnalysisProfileNotFoundException(@NonNull AppleVarietyId varietyId) {
    super("apple variety analysis profile not found: "
        + requireNonNull(varietyId, "apple variety id cannot be null").value());
    this.varietyId = varietyId;
  }

  public @NonNull AppleVarietyId varietyId() {
    return varietyId;
  }
}
