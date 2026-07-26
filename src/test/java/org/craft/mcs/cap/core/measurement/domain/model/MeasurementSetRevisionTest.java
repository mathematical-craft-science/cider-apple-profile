package org.craft.mcs.cap.core.measurement.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class MeasurementSetRevisionTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-20T10:00:00Z");
  private static final MeasurementProvenance PROVENANCE = new MeasurementProvenance(
      MeasurementSourceType.USER,
      MeasurementSourceName.of("Bench hydrometer"),
      null,
      null,
      null
  );

  @Test
  void preservesCanonicalIntervalBoundsWithoutStoredMean() {
    final var value = IntervalCharacteristicValue.of(
        value(MeasurementCharacteristic.SPECIFIC_GRAVITY, new BigDecimal("1.048")),
        value(MeasurementCharacteristic.SPECIFIC_GRAVITY, new BigDecimal("1.052"))
    );

    assertThat(value)
        .satisfies(interval -> assertThat(interval.lower().asBigDecimal())
            .isEqualByComparingTo("1.048"))
        .satisfies(interval -> assertThat(interval.upper().asBigDecimal())
            .isEqualByComparingTo("1.052"));
  }

  @Test
  void rejectsValueObjectThatDoesNotMatchTheCharacteristic() {
    final var sugarValue = point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050");

    assertThatThrownBy(() -> new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        sugarValue,
        MEASURED_AT,
        PROVENANCE,
        null,
        List.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement value type does not match PH");
  }

  @Test
  void requiresOneResultForEveryRepresentedCharacteristic() {
    final var observation = observation(MeasurementCharacteristic.PH);

    assertThatThrownBy(() -> revision(List.of(observation)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("each represented measurement characteristic requires one result record");
  }

  @Test
  void requiresDerivedInputsFromSameCharacteristic() {
    final var observation = observation(MeasurementCharacteristic.PH);
    final var result = new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050"),
        MEASURED_AT,
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(observation.recordId(), MeasurementRecordId.newId())
    );
    final var phResult = new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, "3.40"),
        MEASURED_AT,
        PROVENANCE,
        null,
        List.of()
    );

    assertThatThrownBy(() -> revision(List.of(observation, phResult, result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement inputs must reference observations of the same characteristic");
  }

  @Test
  void acceptsArithmeticMeanThatMatchesReferencedObservations() {
    final var first = observation(MeasurementCharacteristic.PH, "3.20");
    final var second = observation(MeasurementCharacteristic.PH, "3.40");
    final var result = derivedMeanResult("3.30", first, second);

    assertThat(revision(List.of(first, second, result)).records())
        .containsExactly(first, second, result);
  }

  @Test
  void rejectsArithmeticMeanThatDoesNotMatchReferencedObservations() {
    final var first = observation(MeasurementCharacteristic.PH, "3.20");
    final var second = observation(MeasurementCharacteristic.PH, "3.40");
    final var result = derivedMeanResult("3.40", first, second);

    assertThatThrownBy(() -> revision(List.of(first, second, result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement result must equal the declared derivation of its observations");
  }

  @Test
  void requiresDerivedResultToReferenceEveryObservationOfItsCharacteristic() {
    final var first = observation(MeasurementCharacteristic.PH, "3.20");
    final var second = observation(MeasurementCharacteristic.PH, "3.40");
    final var ignored = observation(MeasurementCharacteristic.PH, "3.60");
    final var result = derivedMeanResult("3.30", first, second);

    assertThatThrownBy(() -> revision(List.of(first, second, ignored, result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement result must reference every observation of its characteristic");
  }

  @Test
  void rejectsObservationsWithoutDerivedResult() {
    final var observation = observation(MeasurementCharacteristic.PH, "3.20");
    final var directResult = result(MeasurementCharacteristic.PH);

    assertThatThrownBy(() -> revision(List.of(observation, directResult)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement observations require a derived result");
  }

  @Test
  void rejectsRecordMeasuredAfterRevisionCreation() {
    final var futureResult = result(
        MeasurementCharacteristic.PH,
        MEASURED_AT.plusSeconds(1)
    );

    assertThatThrownBy(() -> revision(List.of(futureResult)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement record time cannot be after its revision creation time");
  }

  @Test
  void requiresDerivedResultTimeToEqualLatestObservationTime() {
    final var first = observation(
        MeasurementCharacteristic.PH,
        "3.20",
        MEASURED_AT.minusSeconds(60)
    );
    final var second = observation(MeasurementCharacteristic.PH, "3.40", MEASURED_AT);
    final var result = derivedMeanResult(
        "3.30",
        MEASURED_AT.minusSeconds(60),
        first,
        second
    );

    assertThatThrownBy(() -> revision(List.of(first, second, result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement result time must equal its latest observation time");
  }

  @Test
  void rejectsCorrectionReasonForInitialRevision() {
    final var result = result(MeasurementCharacteristic.PH);

    assertThatThrownBy(() -> revision(1, "Not an initial revision", List.of(result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("initial measurement revision cannot have a correction reason");
  }

  @Test
  void requiresCorrectionReasonForLaterRevision() {
    final var result = result(MeasurementCharacteristic.PH);

    assertThatThrownBy(() -> revision(2, null, List.of(result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("corrected measurement revision requires a correction reason");
  }

  @Test
  void exposesInitialRevisionNumberFactory() {
    final var revisionId = MeasurementSetRevisionId.of(newUuid());

    assertThat(MeasurementSetRevisionNumber.initial())
        .isEqualTo(MeasurementSetRevisionNumber.of(1))
        .matches(MeasurementSetRevisionNumber::isInitial);
    assertThat(revisionId.value()).isNotNull();
  }

  @Test
  void normalizesOptionalNote() {
    final var result = result(MeasurementCharacteristic.PH);

    assertThat(revisionWithNote("  Verified by second reading  ", List.of(result)).note())
        .isEqualTo("Verified by second reading");
    assertThat(revisionWithNote("   ", List.of(result)).note()).isNull();
    assertThat(revisionWithNote(null, List.of(result)).note()).isNull();
  }

  @Test
  void acceptsMaximumNoteLengthAndRejectsLongerValue() {
    final var result = result(MeasurementCharacteristic.PH);
    final var maximumLengthNote = "n".repeat(2_000);
    final var excessiveNote = "n".repeat(2_001);

    assertThat(revisionWithNote(maximumLengthNote, List.of(result)).note())
        .hasSize(2_000);
    assertThatThrownBy(() -> revisionWithNote(excessiveNote, List.of(result)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement set note length must not exceed 2000 characters");
  }

  private static MeasurementSetRevision revision(List<MeasurementRecord> records) {
    return revision(1, null, records);
  }

  private static MeasurementSetRevision revision(
      int revisionNumber,
      String correctionReason,
      List<MeasurementRecord> records
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(revisionNumber),
        MaterialStateId.newId(),
        MeasurementSetName.of("Laboratory result"),
        null,
        correctionReason == null ? null : MeasurementCorrectionReason.of(correctionReason),
        MEASURED_AT,
        ActorReference.accountUser(newUuid()),
        records
    );
  }

  private static MeasurementSetRevision revisionWithNote(
      String note,
      List<MeasurementRecord> records
  ) {
    return new MeasurementSetRevision(
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.initial(),
        MaterialStateId.newId(),
        MeasurementSetName.of("Laboratory result"),
        note,
        null,
        MEASURED_AT,
        ActorReference.accountUser(newUuid()),
        records
    );
  }

  private static MeasurementRecord observation(MeasurementCharacteristic characteristic) {
    return observation(characteristic, "3.40");
  }

  private static MeasurementRecord observation(
      MeasurementCharacteristic characteristic,
      String measuredValue
  ) {
    return observation(characteristic, measuredValue, MEASURED_AT);
  }

  private static MeasurementRecord observation(
      MeasurementCharacteristic characteristic,
      String measuredValue,
      Instant measuredAt
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.OBSERVATION,
        point(characteristic, measuredValue),
        measuredAt,
        PROVENANCE,
        null,
        List.of()
    );
  }

  private static MeasurementRecord derivedMeanResult(
      String measuredValue,
      MeasurementRecord first,
      MeasurementRecord second
  ) {
    return derivedMeanResult(measuredValue, MEASURED_AT, first, second);
  }

  private static MeasurementRecord derivedMeanResult(
      String measuredValue,
      Instant measuredAt,
      MeasurementRecord first,
      MeasurementRecord second
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementRecordRole.RESULT,
        point(MeasurementCharacteristic.PH, measuredValue),
        measuredAt,
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(first.recordId(), second.recordId())
    );
  }

  private static MeasurementRecord result(MeasurementCharacteristic characteristic) {
    return result(characteristic, MEASURED_AT);
  }

  private static MeasurementRecord result(
      MeasurementCharacteristic characteristic,
      Instant measuredAt
  ) {
    return new MeasurementRecord(
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementRecordRole.RESULT,
        point(characteristic, "3.40"),
        measuredAt,
        PROVENANCE,
        null,
        List.of()
    );
  }

  private static PointCharacteristicValue<MeasuredValue> point(
      MeasurementCharacteristic characteristic,
      String value
  ) {
    return PointCharacteristicValue.of(
        value(characteristic, new BigDecimal(value))
    );
  }
}
