package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv.mapper;

import org.craft.mcs.cap.core.appleprofile.application.abv.port.in.AppleVarietyPotentialAbvPrediction;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv.dto.AppleVarietyPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class AppleVarietyPotentialAbvRestMapper {

  private AppleVarietyPotentialAbvRestMapper() {
  }

  public static @NonNull AppleVarietyPotentialAbvResponse toResponse(
      @NonNull AppleVarietyPotentialAbvPrediction prediction
  ) {
    requireNonNull(prediction, "apple variety potential ABV prediction cannot be null");

    return new AppleVarietyPotentialAbvResponse(
        prediction.varietyId().value(),
        prediction.varietyName().value(),
        ProfileCharacteristicRestMapper.valueKind(prediction.originalGravity()),
        PotentialAbvPredictionRestMapper.toResponse(prediction.prediction())
    );
  }
}
