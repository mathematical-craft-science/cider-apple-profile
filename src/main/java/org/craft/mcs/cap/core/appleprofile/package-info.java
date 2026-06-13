/**
 * Catalog bounded context for pomological apple varieties and their reference chemical profile.
 *
 * <p>This context owns the AppleVariety aggregate as a catalog entity. Regional observations and
 * contributed source-specific profiles belong to {@code core.varietyprofile}. Reusable
 * classification, graph, distance, target and composition algorithms belong to
 * {@code core.cideranalysis}; AppleVariety application slices only adapt catalog profiles to those
 * analysis tools.
 */
@BoundedContext
package org.craft.mcs.cap.core.appleprofile;

import org.jmolecules.ddd.annotation.BoundedContext;


