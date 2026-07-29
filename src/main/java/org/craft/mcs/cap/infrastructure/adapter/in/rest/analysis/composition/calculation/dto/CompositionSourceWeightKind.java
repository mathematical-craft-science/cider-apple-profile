package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How a composition source weight value should be interpreted.")
public enum CompositionSourceWeightKind {

  /** Weight value is an absolute volume in liters. */
  ABSOLUTE_VOLUME_LITERS,

  /** Weight value is a normalized proportion in the range {@code (0, 1]}. */
  PROPORTION
}
