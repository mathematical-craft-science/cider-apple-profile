/**
 * Bounded context for saved production planning candidates.
 *
 * <p>A candidate captures the source composition selected by the technologist, the calculated
 * initial profile, and the source evidence and material states used for that calculation. It can
 * be reviewed independently and confirmed exactly once when a ProductionBatch is created.</p>
 *
 * <p>This subdomain owns candidate identity, lifecycle, and stale-evidence protection. Reusable
 * composition and target-assessment mathematics belong to {@code core.profileanalysis}; source
 * inventory consumption, material trace recording, and the resulting ProductionBatch lifecycle
 * remain responsibilities of the production batch context.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.production.planning;

import org.jmolecules.ddd.annotation.BoundedContext;
