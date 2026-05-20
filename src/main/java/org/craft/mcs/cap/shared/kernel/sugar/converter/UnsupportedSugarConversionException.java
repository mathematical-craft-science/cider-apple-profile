package org.craft.mcs.cap.shared.kernel.sugar.converter;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;


public final class UnsupportedSugarConversionException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedSugarConversionException(
      Class<?> sourceType,
      Class<?> targetType
  ) {
    super("Unsupported sugar conversion: %s -> %s"
        .formatted(sourceType.getSimpleName(), targetType.getSimpleName()));
  }
}
