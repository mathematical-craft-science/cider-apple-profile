package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.appleprofile.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.SugarConcentration;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaEmbeddable;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.measurement.value.PhValue;
import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.fromNullableCharacteristic;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.common.MeasurementJpaMapperSupport.toCharacteristic;

@Entity
@Table(name = "apple_varieties")
@EntityListeners(AuditingEntityListener.class)
public class AppleVarietyJpaEntity {

  @Id
  @NonNull
  @Column(name = "variety_id", nullable = false, updatable = false)
  private UUID varietyId;

  @NonNull
  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Nullable
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "kind", column = @Column(name = "sugar_kind")),
      @AttributeOverride(name = "exactValue", column = @Column(name = "sugar_exact_sg", precision = 12, scale = 4)),
      @AttributeOverride(name = "minValue", column = @Column(name = "sugar_min_sg", precision = 12, scale = 4)),
      @AttributeOverride(name = "maxValue", column = @Column(name = "sugar_max_sg", precision = 12, scale = 4)),
      @AttributeOverride(name = "meanValue", column = @Column(name = "sugar_mean_sg", precision = 12, scale = 4)),
      @AttributeOverride(name = "medianValue", column = @Column(name = "sugar_median_sg", precision = 12, scale = 4)),
      @AttributeOverride(
          name = "standardDeviation",
          column = @Column(name = "sugar_standard_deviation", precision = 12, scale = 4)
      ),
      @AttributeOverride(name = "sampleSize", column = @Column(name = "sugar_sample_size"))
  })
  private MeasurementJpaEmbeddable sugar;

  @Nullable
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "kind", column = @Column(name = "ph_kind")),
      @AttributeOverride(name = "exactValue", column = @Column(name = "ph_exact", precision = 12, scale = 4)),
      @AttributeOverride(name = "minValue", column = @Column(name = "ph_min", precision = 12, scale = 4)),
      @AttributeOverride(name = "maxValue", column = @Column(name = "ph_max", precision = 12, scale = 4)),
      @AttributeOverride(name = "meanValue", column = @Column(name = "ph_mean", precision = 12, scale = 4)),
      @AttributeOverride(name = "medianValue", column = @Column(name = "ph_median", precision = 12, scale = 4)),
      @AttributeOverride(
          name = "standardDeviation",
          column = @Column(name = "ph_standard_deviation", precision = 12, scale = 4)
      ),
      @AttributeOverride(name = "sampleSize", column = @Column(name = "ph_sample_size"))
  })
  private MeasurementJpaEmbeddable ph;

  @Nullable
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "kind", column = @Column(name = "ta_kind")),
      @AttributeOverride(
          name = "exactValue",
          column = @Column(name = "ta_exact_g_l_as_malic", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "minValue",
          column = @Column(name = "ta_min_g_l_as_malic", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "maxValue",
          column = @Column(name = "ta_max_g_l_as_malic", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "meanValue",
          column = @Column(name = "ta_mean_g_l_as_malic", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "medianValue",
          column = @Column(name = "ta_median_g_l_as_malic", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "standardDeviation",
          column = @Column(name = "ta_standard_deviation", precision = 12, scale = 4)
      ),
      @AttributeOverride(name = "sampleSize", column = @Column(name = "ta_sample_size"))
  })
  private MeasurementJpaEmbeddable titratableAcidity;

  @Nullable
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "kind", column = @Column(name = "tanninity_kind")),
      @AttributeOverride(
          name = "exactValue",
          column = @Column(name = "tanninity_exact_mg_l_tae", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "minValue",
          column = @Column(name = "tanninity_min_mg_l_tae", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "maxValue",
          column = @Column(name = "tanninity_max_mg_l_tae", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "meanValue",
          column = @Column(name = "tanninity_mean_mg_l_tae", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "medianValue",
          column = @Column(name = "tanninity_median_mg_l_tae", precision = 12, scale = 4)
      ),
      @AttributeOverride(
          name = "standardDeviation",
          column = @Column(name = "tanninity_standard_deviation", precision = 12, scale = 4)
      ),
      @AttributeOverride(name = "sampleSize", column = @Column(name = "tanninity_sample_size"))
  })
  private MeasurementJpaEmbeddable tanninity;

  @NonNull
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @NonNull
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @SuppressWarnings("NullAway.Init")
  protected AppleVarietyJpaEntity() {
  }

  private AppleVarietyJpaEntity(
      @NonNull UUID varietyId,
      @NonNull String name,
      @Nullable MeasurementJpaEmbeddable sugar,
      @Nullable MeasurementJpaEmbeddable ph,
      @Nullable MeasurementJpaEmbeddable titratableAcidity,
      @Nullable MeasurementJpaEmbeddable tanninity
  ) {
    this.varietyId = requireNonNull(varietyId, "variety id cannot be null");
    this.name = requireNonNull(name, "apple variety name cannot be null");
    this.sugar = sugar;
    this.ph = ph;
    this.titratableAcidity = titratableAcidity;
    this.tanninity = tanninity;
  }

  public static @NonNull AppleVarietyJpaEntity fromDomain(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var profile = appleVariety.chemicalProfile();

    return new AppleVarietyJpaEntity(
        appleVariety.varietyId().value(),
        appleVariety.name().value(),
        fromNullableCharacteristic(
            profile.sugar().orElse(null),
            sugar -> sugar.specificGravity().asBigDecimal()
        ),
        fromNullableCharacteristic(
            profile.ph().orElse(null),
            PhValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.titratableAcidity().orElse(null),
            TitratableAcidityValue::asBigDecimal
        ),
        fromNullableCharacteristic(
            profile.tanninity().orElse(null),
            TanninityValue::asBigDecimal
        )
    );
  }

  public @NonNull AppleVariety toDomain() {
    final var builder = ChemicalProfile.builder();

    toCharacteristic(
        sugar,
        value -> SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(value.toPlainString()))
    ).ifPresent(builder::sugar);

    toCharacteristic(
        ph,
        value -> PhValue.of(value.toPlainString())
    ).ifPresent(builder::ph);

    toCharacteristic(
        titratableAcidity,
        value -> TitratableAcidityValue.of(value.toPlainString())
    ).ifPresent(builder::titratableAcidity);

    toCharacteristic(
        tanninity,
        value -> TanninityValue.of(value.toPlainString())
    ).ifPresent(builder::tanninity);

    return AppleVariety.create(
        AppleVarietyId.of(requireNonNull(varietyId, "variety id cannot be null")),
        AppleVarietyName.of(requireNonNull(name, "apple variety name cannot be null")),
        builder.build()
    );
  }

  @NonNull
  UUID id() {
    return requireNonNull(varietyId, "variety id cannot be null");
  }
}