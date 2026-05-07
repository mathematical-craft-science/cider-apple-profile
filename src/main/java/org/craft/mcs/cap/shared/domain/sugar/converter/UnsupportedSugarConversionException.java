package org.craft.mcs.cap.shared.domain.sugar.converter;

public final class UnsupportedSugarConversionException extends RuntimeException {

  public UnsupportedSugarConversionException(
      Class<?> sourceType,
      Class<?> targetType
  ) {
    super("Unsupported sugar conversion: %s -> %s"
        .formatted(sourceType.getSimpleName(), targetType.getSimpleName()));
  }
}
