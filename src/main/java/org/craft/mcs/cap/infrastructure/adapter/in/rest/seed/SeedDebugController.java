package org.craft.mcs.cap.infrastructure.adapter.in.rest.seed;

import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.AppleVarietyJsonSeedImporter;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNull;


@RestController
@Profile({"dev", "local"})
public final class SeedDebugController {

  private static final @NonNull String APPLE_VARIETIES_SEED_RESOURCE =
      "db/seed/apple-varieties.json";

  private final @NonNull AppleVarietyJsonSeedImporter appleVarietyJsonSeedImporter;

  public SeedDebugController(@NonNull AppleVarietyJsonSeedImporter appleVarietyJsonSeedImporter) {
    this.appleVarietyJsonSeedImporter = requireNonNull(
        appleVarietyJsonSeedImporter,
        "apple variety json seed importer cannot be null"
    );
  }

  @PostMapping("/debug/seed/apple-varieties")
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

  public record SeedImportResponse(@NonNull String message) {

    public SeedImportResponse {
      requireNonNull(message, "seed import response message cannot be null");
    }
  }
}
