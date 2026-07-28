/**
 * Bounded context for raw material lots available to cider-production planning.
 *
 * <p>Feedstock owns FeedstockBatch identity, source metadata, season, type and available material
 * state. Generic measurement evidence, cross-context lineage and reusable profile analysis remain
 * in their dedicated contexts. Feedstock application slices compose those boundaries without
 * transferring aggregate ownership.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.feedstock;

import org.jmolecules.ddd.annotation.BoundedContext;
