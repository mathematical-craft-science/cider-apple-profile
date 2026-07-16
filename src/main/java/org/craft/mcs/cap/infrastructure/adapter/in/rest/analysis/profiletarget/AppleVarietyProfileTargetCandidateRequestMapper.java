package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.AppleVarietyProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.mapper.AppleVarietyProfileTargetCandidateRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Maps AppleVariety target-candidate requests and resolves predefined targets at the REST boundary. */
@Component
@RequiredArgsConstructor
public final class AppleVarietyProfileTargetCandidateRequestMapper {

  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @NonNull AppleVarietyProfileTargetCandidateQuery toQuery(
      @NonNull AppleVarietyProfileTargetCandidateSearchRequest request
  ) {
    return AppleVarietyProfileTargetCandidateRestMapper.toQuery(request, targetResolver);
  }
}
