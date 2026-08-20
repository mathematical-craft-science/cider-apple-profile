package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps shared target transport values to Feedstock target selections. */
public final class FeedstockProfileTargetRestMapper {

  private FeedstockProfileTargetRestMapper() {
  }

  public static @NonNull FeedstockProfileTargetSelection toSelection(
      @NonNull ProfileTargetRequest request
  ) {
    requireNonNull(request, "feedstock profile target request cannot be null");
    return switch (request) {
      case PredefinedZoneProfileTargetRequest(_, var zone) ->
          new PredefinedFeedstockProfileTargetSelection(
              Zone.valueOf(zone.name())
          );
      case CustomRectangleProfileTargetRequest customRectangle ->
          new CustomRectangleFeedstockProfileTargetSelection(
              MeasuredValueRange.closed(
                  SpecificGravityValue.of(customRectangle.specificGravityMin()),
                  SpecificGravityValue.of(customRectangle.specificGravityMax())
              ),
              MeasuredValueRange.closed(
                  TitratableAcidityValue.of(customRectangle.titratableAcidityMin()),
                  TitratableAcidityValue.of(customRectangle.titratableAcidityMax())
              )
          );
    };
  }

  public static @Nullable FeedstockProfileTargetSelection toOptionalSelection(
      @Nullable ProfileTargetRequest request
  ) {
    return request == null ? null : toSelection(request);
  }

  public static @Nullable MeasuredValueRange<TanninValue> toTanninTarget(
      @Nullable ProfileTargetTanninRangeRequest request
  ) {
    return request == null
        ? null
        : MeasuredValueRange.closed(TanninValue.of(request.min()), TanninValue.of(request.max()));
  }
}
