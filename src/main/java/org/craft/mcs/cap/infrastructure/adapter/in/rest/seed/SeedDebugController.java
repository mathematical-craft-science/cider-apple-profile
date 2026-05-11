package org.craft.mcs.cap.infrastructure.adapter.in.rest.seed;

import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.AppleVarietyJsonSeedImporter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.seed.dto.SeedImportResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Profile({"dev", "local"})
public final class SeedDebugController implements SeedDebugApi {

  private static final @NonNull String APPLE_VARIETIES_SEED_RESOURCE =
      "db/seed/apple-varieties.json";

  private final @NonNull AppleVarietyJsonSeedImporter appleVarietyJsonSeedImporter;

  public SeedDebugController(@NonNull AppleVarietyJsonSeedImporter appleVarietyJsonSeedImporter) {
    this.appleVarietyJsonSeedImporter = requireNonNull(
        appleVarietyJsonSeedImporter,
        "apple variety json seed importer cannot be null"
    );
  }

  @Override
  @PostMapping(value = "/debug/seed/apple-varieties", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull SeedImportResponse> importAppleVarieties() {
    final var resource = new ClassPathResource(APPLE_VARIETIES_SEED_RESOURCE);

    if (!resource.exists()) {
      return ResponseEntity
          .badRequest()
          .body(new SeedImportResponse("Seed resource not found: " + APPLE_VARIETIES_SEED_RESOURCE));
    }

    try (var inputStream = resource.getInputStream()) {
      appleVarietyJsonSeedImporter.importFrom(inputStream);

      return ResponseEntity.ok(
          new SeedImportResponse("Apple variety seed imported successfully")
      );
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot import apple variety seed", exception);
    }
  }

}
