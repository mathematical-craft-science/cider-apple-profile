package org.craft.mcs.cap.core.appleprofile.catalog.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.jspecify.annotations.NonNull;


public interface AppleVarietyReferenceProfilePageProvider {

  @NonNull
  List<@NonNull AppleVarietyReferenceProfileView> findProfiles(
      @NonNull AppleVarietyReferenceProfilePageQuery query
  );
}
