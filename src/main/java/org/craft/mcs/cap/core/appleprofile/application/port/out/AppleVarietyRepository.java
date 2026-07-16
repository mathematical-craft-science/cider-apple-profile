package org.craft.mcs.cap.core.appleprofile.application.port.out;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;

public interface AppleVarietyRepository
    extends VersionedAggregateRepository<AppleVariety, AppleVarietyId> {
}
