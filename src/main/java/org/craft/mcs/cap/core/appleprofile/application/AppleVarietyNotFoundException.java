package org.craft.mcs.cap.core.appleprofile.application;

import java.io.Serial;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class AppleVarietyNotFoundException
    extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull AppleVarietyId varietyId;

  public AppleVarietyNotFoundException(@NonNull AppleVarietyId varietyId) {
    super("apple variety not found: " + requireNonNull(varietyId, "apple variety id cannot be null").value());
    this.varietyId = varietyId;
  }

  public @NonNull AppleVarietyId varietyId() {
    return varietyId;
  }
}
