package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel;

import java.util.List;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselUseCase;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(VesselController.class)
@AutoConfigureMockMvc(addFilters = false)
class VesselControllerWebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CreateVesselUseCase createVesselUseCase;

  @MockitoBean
  private GetVesselUseCase getVesselUseCase;

  @MockitoBean
  private ListAvailableVesselsUseCase listAvailableVesselsUseCase;

  @MockitoBean
  private ListVesselsUseCase listVesselsUseCase;

  @MockitoBean
  private UpdateVesselUseCase updateVesselUseCase;

  @MockitoBean
  private EnsureCurrentAccountProvisionedUseCase ensureCurrentAccountProvisionedUseCase;

  @Test
  void validatesCreateRequestThroughApiInterfaceConstraints() throws Exception {
    mockMvc.perform(post("/vessels")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "workspaceId": "019f818a-7008-7000-9ef5-3381945e3c41",
                  "name": "",
                  "nominalCapacityLiters": 0,
                  "material": "FIBERGLASS"
                }
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void listsAvailableVesselsWithoutRequiredVolume() throws Exception {
    when(listAvailableVesselsUseCase.listAvailable(any())).thenReturn(List.of());

    mockMvc.perform(get("/vessels/available")
            .param("workspaceId", "019f818a-7008-7000-9ef5-3381945e3c41"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vessels").isEmpty());
  }

  @Test
  void listsAvailableVesselsWithPositiveRequiredVolume() throws Exception {
    when(listAvailableVesselsUseCase.listAvailable(any())).thenReturn(List.of());

    mockMvc.perform(get("/vessels/available")
            .param("workspaceId", "019f818a-7008-7000-9ef5-3381945e3c41")
            .param("requiredVolumeLiters", "250.000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vessels").isEmpty());

    verify(listAvailableVesselsUseCase).listAvailable(argThat(query ->
        VolumeValue.ofLiters("250.000").equals(query.requiredVolume())
    ));
  }
}
