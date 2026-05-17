/**
 * Inbound application ports and immutable results for the feedstock workspace.
 *
 * <p>The package separates operational batch use cases, measurement-history use cases, current
 * batch assessment, potential ABV prediction, target scoring and composition optimization. Commands
 * represent user intent at the application boundary; services enforce ownership and current-profile
 * semantics before delegating to domain logic.
 */
//@org.jspecify.annotations.NullMarked
package org.craft.mcs.cap.core.feedstock.application.port.in;
