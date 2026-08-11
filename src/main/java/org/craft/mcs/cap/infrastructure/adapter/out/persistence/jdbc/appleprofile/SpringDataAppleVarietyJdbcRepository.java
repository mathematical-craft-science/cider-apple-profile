package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity.AppleVarietyJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.common.MeasurementKind;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


interface SpringDataAppleVarietyJdbcRepository
    extends ListCrudRepository<AppleVarietyJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE apple_varieties
         SET name = :name,
             sugar_kind = :sugarKind,
             sugar_exact_sg = :sugarExact,
             sugar_min_sg = :sugarMin,
             sugar_max_sg = :sugarMax,
             ph_kind = :phKind,
             ph_exact = :phExact,
             ph_min = :phMin,
             ph_max = :phMax,
             ta_kind = :acidityKind,
             ta_exact_g_l_as_malic = :acidityExact,
             ta_min_g_l_as_malic = :acidityMin,
             ta_max_g_l_as_malic = :acidityMax,
             tannin_kind = :tanninKind,
             tannin_exact_mg_l_tae = :tanninExact,
             tannin_min_mg_l_tae = :tanninMin,
             tannin_max_mg_l_tae = :tanninMax,
             chemical_profile_source = :referenceProfileSource,
             chemical_profile_confidence = :referenceProfileConfidence,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE variety_id = :varietyId
         AND version = :expectedVersion
      """)
  @SuppressWarnings("java:S107")
  int updateByIdAndVersion(
      @Param("varietyId") @NonNull UUID varietyId,
      @Param("name") @NonNull String name,
      @Param("sugarKind") @Nullable MeasurementKind sugarKind,
      @Param("sugarExact") @Nullable BigDecimal sugarExact,
      @Param("sugarMin") @Nullable BigDecimal sugarMin,
      @Param("sugarMax") @Nullable BigDecimal sugarMax,
      @Param("phKind") @Nullable MeasurementKind phKind,
      @Param("phExact") @Nullable BigDecimal phExact,
      @Param("phMin") @Nullable BigDecimal phMin,
      @Param("phMax") @Nullable BigDecimal phMax,
      @Param("acidityKind") @Nullable MeasurementKind acidityKind,
      @Param("acidityExact") @Nullable BigDecimal acidityExact,
      @Param("acidityMin") @Nullable BigDecimal acidityMin,
      @Param("acidityMax") @Nullable BigDecimal acidityMax,
      @Param("tanninKind") @Nullable MeasurementKind tanninKind,
      @Param("tanninExact") @Nullable BigDecimal tanninExact,
      @Param("tanninMin") @Nullable BigDecimal tanninMin,
      @Param("tanninMax") @Nullable BigDecimal tanninMax,
      @Param("referenceProfileSource") @Nullable String referenceProfileSource,
      @Param("referenceProfileConfidence") @Nullable ConfidenceLevel referenceProfileConfidence,
      @Param("expectedVersion") long expectedVersion
  );
}
