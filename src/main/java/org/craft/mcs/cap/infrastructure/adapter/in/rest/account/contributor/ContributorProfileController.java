package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ContributorProfileUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.dto.ConfigureContributorProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.dto.ContributorProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.mapper.ContributorProfileRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST adapter for current-account contributor profile management.
 */
@RestController
@RequiredArgsConstructor
public final class ContributorProfileController
    implements ContributorProfileApi {

  private final @NonNull ContributorProfileUseCase contributorProfileUseCase;

  @Override
  @GetMapping("/account/contributor-profile")
  public @NonNull ResponseEntity<@NonNull ContributorProfileResponse> contributorProfile() {
    return ResponseEntity.ok(ContributorProfileRestMapper.toResponse(contributorProfileUseCase.get()));
  }

  @Override
  @PutMapping("/account/contributor-profile")
  public @NonNull ResponseEntity<@NonNull ContributorProfileResponse> configureContributorProfile(
      @RequestBody @NonNull ConfigureContributorProfileRequest request
  ) {
    return ResponseEntity.ok(ContributorProfileRestMapper.toResponse(
        contributorProfileUseCase.configure(ContributorProfileRestMapper.toCommand(request))
    ));
  }

  @Override
  @DeleteMapping("/account/contributor-profile")
  public @NonNull ResponseEntity<@NonNull Void> deleteContributorProfile() {
    contributorProfileUseCase.delete();
    return ResponseEntity.noContent().build();
  }
}
