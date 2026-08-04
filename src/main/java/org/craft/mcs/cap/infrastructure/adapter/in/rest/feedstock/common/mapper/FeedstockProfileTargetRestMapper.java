package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
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
          new PredefinedFeedstockProfileTargetSelection(zone);
      case CustomRectangleProfileTargetRequest customRectangle ->
          new CustomRectangleFeedstockProfileTargetSelection(
              ProfileTargetRestMapper.toRectangleGeometry(customRectangle)
          );
    };
  }

  public static @Nullable FeedstockProfileTargetSelection toOptionalSelection(
      @Nullable ProfileTargetRequest request
  ) {
    return request == null ? null : toSelection(request);
  }

  public static @Nullable TanninTargetRange toTanninTarget(
      @Nullable ProfileTargetTanninRangeRequest request
  ) {
    return request == null ? null : ProfileTargetRestMapper.toTanninTarget(request);
  }
}
