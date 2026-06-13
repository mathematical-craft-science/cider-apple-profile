/**
 * Catalog application ports for maintaining apple varieties as reference entities.
 *
 * <p>This slice owns commands/results for the variety catalog itself. It does not perform cider
 * analysis; graph, target, classification and ABV scenarios use the catalog profile as input from
 * their own application slices.
 */
package org.craft.mcs.cap.core.appleprofile.application.catalog.port.in;
