package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.mapper.AppleVarietyProfileTargetCandidateRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Maps AppleVariety target-candidate requests and resolves predefined targets at the REST boundary. */
@Component
public final class AppleVarietyProfileTargetCandidateRequestMapper {

  @NonNull AppleVarietyProfileTargetCandidateQuery toQuery(
      @NonNull AppleVarietyProfileTargetCandidateSearchRequest request
  ) {
    return AppleVarietyProfileTargetCandidateRestMapper.toQuery(request);
  }
}
