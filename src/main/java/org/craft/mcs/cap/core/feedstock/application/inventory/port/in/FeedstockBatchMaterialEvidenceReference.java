package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;


/**
 * Stable token for the complete set of measurement records used by a Feedstock material profile.
 *
 * <p>The token changes whenever any selected characteristic starts using different evidence. It
 * therefore detects corrections, retractions and partial measurements without treating one
 * arbitrarily selected measurement set as the profile's source of truth.</p>
 */
public record FeedstockBatchMaterialEvidenceReference(@NonNull String value) {

  private static final String FORMAT_PREFIX = "feedstock-profile-v1:";
  private static final int MAX_LENGTH = 160;

  public FeedstockBatchMaterialEvidenceReference {
    value = requireNonNull(value, "feedstock material evidence reference cannot be null").strip();
    if (!value.startsWith(FORMAT_PREFIX) || value.length() == FORMAT_PREFIX.length()) {
      throw new ApplicationValidationException("feedstock material evidence reference has an invalid format");
    }
    if (value.length() > MAX_LENGTH) {
      throw new ApplicationValidationException("feedstock material evidence reference is too long");
    }
  }

  public static @NonNull FeedstockBatchMaterialEvidenceReference from(
      @NonNull List<@NonNull MeasurementEvidenceSnapshot> evidence
  ) {
    evidence = List.copyOf(requireNonNull(evidence, "feedstock profile evidence cannot be null"));
    if (evidence.isEmpty()) {
      throw new ApplicationValidationException("feedstock material profile must reference measurement evidence");
    }
    final var distinctCharacteristicCount = evidence.stream()
        .map(MeasurementEvidenceSnapshot::characteristic)
        .distinct()
        .count();
    if (distinctCharacteristicCount != evidence.size()) {
      throw new ApplicationValidationException(
          "feedstock material profile must reference one evidence record per characteristic"
      );
    }
    final var encodedRecordIds = evidence.stream()
        .sorted(Comparator.comparing(MeasurementEvidenceSnapshot::characteristic))
        .map(MeasurementEvidenceSnapshot::evidenceReference)
        .map(MeasurementEvidenceReference::recordId)
        .map(FeedstockBatchMaterialEvidenceReference::encode)
        .collect(joining(","));
    return new FeedstockBatchMaterialEvidenceReference(FORMAT_PREFIX + encodedRecordIds);
  }

  public @NonNull String externalValue() {
    return value;
  }

  private static @NonNull String encode(@NonNull UUID value) {
    final var bytes = ByteBuffer.allocate(Long.BYTES * 2)
        .putLong(value.getMostSignificantBits())
        .putLong(value.getLeastSignificantBits())
        .array();
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
