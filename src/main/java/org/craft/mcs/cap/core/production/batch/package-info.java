/**
 * Bounded context for confirmed production batches and their technological lifecycle.
 *
 * <p>The context owns ProductionBatch material, source consumption and creation invariants.
 * Candidate selection and confirmation status belong to {@code production.planning}.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.production.batch;

import org.jmolecules.ddd.annotation.BoundedContext;
