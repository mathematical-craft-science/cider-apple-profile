package org.craft.mcs.cap.core.appleprofile.catalog.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyReferenceProfileView;
import org.jspecify.annotations.NonNull;


/** Published catalog query for ordered pages of immutable reference profiles. */
public interface QueryAppleVarietyReferenceProfilePageUseCase {

  @NonNull
  List<@NonNull AppleVarietyReferenceProfileView> query(
      @NonNull AppleVarietyReferenceProfilePageQuery query
  );
}
