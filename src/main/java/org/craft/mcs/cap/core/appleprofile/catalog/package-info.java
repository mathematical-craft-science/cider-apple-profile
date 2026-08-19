/**
 * Catalog bounded context for pomological apple varieties and typed reference characteristics.
 *
 * <p>This context owns the AppleVariety aggregate and publishes immutable reference profiles for
 * consumers. Regional observations and source-specific contributed profiles belong to
 * {@code core.varietyprofile}.</p>
 */
@BoundedContext
package org.craft.mcs.cap.core.appleprofile.catalog;

import org.jmolecules.ddd.annotation.BoundedContext;
