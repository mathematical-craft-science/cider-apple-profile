package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.QueryAppleVarietyProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.dto.AppleVarietyProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.applevariety.mapper.AppleVarietyProfileTargetCandidateRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.mapper.FeedstockBatchProfileTargetCandidateRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class ProfileTargetCandidateController implements ProfileTargetCandidateApi {

  private final @NonNull QueryAppleVarietyProfileTargetCandidatesUseCase queryAppleVarietyCandidatesUseCase;
  private final @NonNull QueryFeedstockBatchProfileTargetCandidatesUseCase queryFeedstockBatchCandidatesUseCase;
  private final @NonNull SugarAcidityTargetResolver targetResolver;

  @Override
  @PostMapping(
      value = "/analysis/apple-varieties/profile-target-candidates",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull AppleVarietyProfileTargetCandidatesResponse> appleVarietyCandidates(
      @RequestBody @NonNull AppleVarietyProfileTargetCandidateSearchRequest request
  ) {
    final var candidates = queryAppleVarietyCandidatesUseCase.query(
        AppleVarietyProfileTargetCandidateRestMapper.toQuery(request, targetResolver)
    );

    return ResponseEntity.ok(AppleVarietyProfileTargetCandidateRestMapper.toResponse(candidates));
  }

  @Override
  @PostMapping(
      value = "/analysis/feedstock-batches/profile-target-candidates",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull FeedstockBatchProfileTargetCandidatesResponse> feedstockBatchCandidates(
      @RequestBody @NonNull FeedstockBatchProfileTargetCandidateSearchRequest request
  ) {
    final var candidates = queryFeedstockBatchCandidatesUseCase.query(
        FeedstockBatchProfileTargetCandidateRestMapper.toQuery(request)
    );

    return ResponseEntity.ok(FeedstockBatchProfileTargetCandidateRestMapper.toResponse(candidates));
  }
}
