package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityGraphMetadataResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** Serves metadata for the configured Jolicoeur sugar-acidity graph. */
@RestController
@RequiredArgsConstructor
public final class JolicoeurSugarAcidityGraphMetadataController
    implements JolicoeurSugarAcidityGraphMetadataApi {

  private final @NonNull JolicoeurSugarAcidityGraphMetadataResponseProvider responseProvider;

  @Override
  @GetMapping(
      value = "/profile-analysis/graphs/jolicoeur-sugar-acidity/metadata",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull JolicoeurSugarAcidityGraphMetadataResponse>
      jolicoeurSugarAcidityGraphMetadata() {
    return ResponseEntity.ok(responseProvider.get());
  }
}
