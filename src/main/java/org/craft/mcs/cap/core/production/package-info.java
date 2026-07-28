/**
 * Bounded context for confirmed production lots and their technological lifecycle.
 *
 * <p>Production owns ProductionBatch identity, material state, initial accepted profile and
 * alcoholic-fermentation state. Source contexts remain responsible for their own material
 * inventories; Production consumes them only through source boundaries. Measurements and
 * cross-context lineage belong to their dedicated bounded contexts.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.production;

import org.jmolecules.ddd.annotation.BoundedContext;
