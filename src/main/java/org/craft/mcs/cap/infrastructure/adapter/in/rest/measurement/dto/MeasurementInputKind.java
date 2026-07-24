package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/** Transport discriminator for direct values and repeated-observation derivations. */
@Schema(
    description = "How one characteristic is supplied: directly or as repeated observations.",
    enumAsRef = true
)
public enum MeasurementInputKind {

  /** One reported POINT or INTERVAL is converted at ingress and becomes the working RESULT directly. */
  DIRECT,

  /** Several immutable POINT observations produce a separate derived working RESULT. */
  REPEATED
}
