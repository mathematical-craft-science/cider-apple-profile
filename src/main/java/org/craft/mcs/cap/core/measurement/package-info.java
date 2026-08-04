/**
 * Bounded context for immutable measurement evidence and state-aware current-value projection.
 *
 * <p>Measurement records facts about a referenced material subject without owning Feedstock or
 * Production lifecycles. Subject existence and access are resolved through application boundaries,
 * while canonical characteristic values come from the shared kernel.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.measurement;

import org.jmolecules.ddd.annotation.BoundedContext;
