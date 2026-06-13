package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How a composition source weight value should be interpreted.")
public enum ChemicalProfileCompositionSourceWeightKind {
  ABSOLUTE_VOLUME_LITERS,
  PROPORTION
}
