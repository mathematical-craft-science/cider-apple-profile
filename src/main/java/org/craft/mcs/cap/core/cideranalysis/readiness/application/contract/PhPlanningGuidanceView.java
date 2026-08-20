package org.craft.mcs.cap.core.cideranalysis.readiness.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Published pH planning guidance. */
public record PhPlanningGuidanceView(
    @NonNull Code code,
    @NonNull String message,
    @Nullable BigDecimal value,
    @NonNull String sourceCode
) {

  public PhPlanningGuidanceView {
    requireNonNull(code, "pH planning guidance code cannot be null");
    requireNonNull(message, "pH planning guidance message cannot be null");
    requireNonNull(sourceCode, "pH planning guidance source code cannot be null");
    if (message.isBlank() || sourceCode.isBlank()) {
      throw new ApplicationValidationException("pH planning guidance text fields cannot be blank");
    }
    if ((code == Code.PH_MISSING) != (value == null)) {
      throw new ApplicationValidationException("pH planning guidance value must match its code");
    }
  }

  public enum Code {
    PH_PLANNING_IDEAL,
    PH_PLANNING_LOW,
    PH_PLANNING_HIGH,
    PH_MISSING
  }
}
