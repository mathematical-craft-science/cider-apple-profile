/**
 * Bounded context for Feedstock batches and their current source-material state.
 *
 * <p>The context owns FeedstockBatch lifecycle, inventory, vessel assignment, state-aware current
 * evidence and single-batch assessments. Comparative scoring and composition planning belong to
 * {@code feedstock.planning}.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.feedstock.batch;

import org.jmolecules.ddd.annotation.BoundedContext;
