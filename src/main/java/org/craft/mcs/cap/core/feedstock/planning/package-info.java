/**
 * Bounded context for comparing Feedstock batches and planning source compositions.
 *
 * <p>The context owns Planning Board projections, candidate selection, target scoring and
 * composition optimization. It consumes immutable batch snapshots through its own outbound ports
 * and does not own FeedstockBatch lifecycle or persistence.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.feedstock.planning;

import org.jmolecules.ddd.annotation.BoundedContext;
