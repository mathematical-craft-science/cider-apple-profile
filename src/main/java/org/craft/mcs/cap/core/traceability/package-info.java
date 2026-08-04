/**
 * Bounded context for immutable material lineage across independently owned subjects.
 *
 * <p>Traceability records historical steps, state references and snapshots. It does not own
 * Feedstock or Production lifecycle invariants and is not their concurrency authority.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.traceability;

import org.jmolecules.ddd.annotation.BoundedContext;
