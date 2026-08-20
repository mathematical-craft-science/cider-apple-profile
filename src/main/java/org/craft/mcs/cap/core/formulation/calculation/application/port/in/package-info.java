/**
 * Inbound ports for calculating typed characteristics from an explicitly chosen source composition.
 *
 * <p>Calculation ports are scenario-neutral: callers provide sources, weights and optional target
 * scoring configuration, while the application service selects the calculator and returns the
 * calculated characteristics plus explainable target distance when requested.
 */
@org.jspecify.annotations.NullMarked
package org.craft.mcs.cap.core.formulation.calculation.application.port.in;
