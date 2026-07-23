package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Vessel, tank or container currently holding a material batch.
 *
 * <p>This shared value object represents the vessel known to material-owning contexts without
 * assigning ownership of an equipment catalog to either Feedstock or Production.
 */
@ValueObject
public record Vessel(@NonNull String name) {

  private static final int MAX_NAME_LENGTH = 160;

  public Vessel {
    requireNonNull(name, "vessel name cannot be null");
    name = name.strip();

    if (name.isBlank()) {
      throw new DomainRuleViolationException("vessel name cannot be blank");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new DomainRuleViolationException(
          "vessel name length must not exceed " + MAX_NAME_LENGTH + " characters"
      );
    }
  }

  public static @NonNull Vessel named(@NonNull String name) {
    return new Vessel(name);
  }
}
