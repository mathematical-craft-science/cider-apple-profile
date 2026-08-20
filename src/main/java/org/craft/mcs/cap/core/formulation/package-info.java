/**
 * Stateless formulation bounded context for composition calculation and optimization.
 *
 * <p>The context works with immutable source snapshots and never owns material lifecycle,
 * persistence transactions, or concurrency control.</p>
 */
@BoundedContext
@NullMarked
package org.craft.mcs.cap.core.formulation;

import org.jmolecules.ddd.annotation.BoundedContext;
import org.jspecify.annotations.NullMarked;
