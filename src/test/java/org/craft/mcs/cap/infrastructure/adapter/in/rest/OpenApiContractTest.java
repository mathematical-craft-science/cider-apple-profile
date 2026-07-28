package org.craft.mcs.cap.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.craft.mcs.cap.core.account.audit.application.port.in.ListWorkspaceAdministrativeAuditUseCase;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ContributorProfileUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.GetIncomingWorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.ListIncomingWorkspaceInvitationsUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.administration.application.port.in.ListWorkspaceMembersUseCase;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMembershipManagementUseCase;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.GetWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceLifecycleUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.appleprofile.application.abv.port.in.PredictAppleVarietyPotentialAbvUseCase;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.ClassifyAppleVarietyNorthAmericanUseCase;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in.*;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.*;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryBatchTraceabilityUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.*;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.*;
import org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in.*;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.*;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.DeleteFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.CorrectMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.GetCurrentMeasurementSubjectStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.QueryFeedstockPlanningBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.*;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.in.GetProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.*;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.*;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.AppleVarietyJsonSeedImporter;
import org.craft.mcs.cap.infrastructure.adapter.in.measurement.MeasurementValueInputAcl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.ChemicalProfileCompositionRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.AppleVarietyProfileTargetCandidateRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper.AppleVarietyProfileGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper.FeedstockBatchSugarAcidityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper.FeedstockMeasurementProfileGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.FeedstockMeasurementCompatibilityFacade;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.MeasurementSetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper.ProductionBatchCandidateRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.SugarAcidityGraphMetadataResponseProvider;
import org.craft.mcs.cap.infrastructure.configuration.OpenApiConfiguration;
import org.craft.mcs.cap.infrastructure.configuration.openapi.DecimalMinOpenApiCustomizer;
import org.craft.mcs.cap.infrastructure.configuration.openapi.PolymorphicSchemaFlatteningCustomizer;
import org.craft.mcs.cap.infrastructure.configuration.openapi.RequiredNullableOpenApiCustomizer;
import org.craft.mcs.cap.infrastructure.configuration.openapi.RestDtoSchemaTypeScanner;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocJavadocConfiguration;
import org.springdoc.core.configuration.SpringDocSecurityConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.infrastructure.http.ActiveWorkspaceHttpHeaders.ACTIVE_WORKSPACE_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(SpringDocConfigProperties.class)
@Import({
    OpenApiConfiguration.class,
    SpringDocConfiguration.class,
    SpringDocJavadocConfiguration.class,
    SpringDocSecurityConfiguration.class,
    SpringDocWebMvcConfiguration.class,
    DecimalMinOpenApiCustomizer.class,
    PolymorphicSchemaFlatteningCustomizer.class,
    RequiredNullableOpenApiCustomizer.class,
    RestDtoSchemaTypeScanner.class,
    ChemicalProfileCompositionRequestMapper.class,
    AppleVarietyProfileTargetCandidateRequestMapper.class,
    ProductionBatchCandidateRestMapper.class,
    SugarAcidityGraphMetadataResponseProvider.class,
    MeasurementValueInputAcl.class,
    MeasurementSetRestMapper.class
})
@MockitoBean(types = {
    ContributorProfileUseCase.class,
    AppleVarietyProfileGraphRestMapper.class,
    FeedstockBatchSugarAcidityRestMapper.class,
    FeedstockMeasurementProfileGraphRestMapper.class,
    EnsureCurrentAccountProvisionedUseCase.class,
    GetIncomingWorkspaceInvitationUseCase.class,
    ListIncomingWorkspaceInvitationsUseCase.class,
    GetCurrentAccountProfileUseCase.class,
    GetCurrentAccountUseCase.class,
    GetCurrentUserPreferencesUseCase.class,
    GetWorkspaceSettingsUseCase.class,
    ListAccountWorkspacesUseCase.class,
    ListWorkspaceMembersUseCase.class,
    ListWorkspaceAdministrativeAuditUseCase.class,
    UpdateAccountProfileUseCase.class,
    UpdateUserPreferencesUseCase.class,
    UpdateWorkspaceSettingsUseCase.class,
    WorkspaceSettingsReader.class,
    WorkspaceInvitationUseCase.class,
    WorkspaceLifecycleUseCase.class,
    WorkspaceMembershipManagementUseCase.class,
    PredictAppleVarietyPotentialAbvUseCase.class,
    ClassifyAppleVarietyNorthAmericanUseCase.class,
    ClassifyAppleVarietyUseCase.class,
    QueryAppleVarietyProfileTargetCandidatesUseCase.class,
    AssessAppleVarietySugarAcidityZoneUseCase.class,
    QueryAppleVarietySugarAcidityZonePointsUseCase.class,
    QueryBatchTraceabilityUseCase.class,
    ProjectCurrentMaterialStateUseCase.class,
    PredictFeedstockBatchCurrentPotentialAbvUseCase.class,
    PredictFeedstockMeasurementPotentialAbvUseCase.class,
    QueryFeedstockBatchProfileTargetCandidatesUseCase.class,
    ScoreFeedstockBatchProfileTargetUseCase.class,
    AssessFeedstockBatchCurrentFermentationStartReadinessUseCase.class,
    AssessFeedstockBatchCurrentSugarAcidityZoneUseCase.class,
    AssessFeedstockMeasurementSugarAcidityZoneUseCase.class,
    QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase.class,
    QueryFeedstockMeasurementSugarAcidityZonePointsUseCase.class,
    CreateFeedstockBatchUseCase.class,
    DeleteFeedstockBatchUseCase.class,
    ListFeedstockBatchesUseCase.class,
    UpdateFeedstockBatchUseCase.class,
    GetFeedstockBatchDetailUseCase.class,
    CreateMeasurementSetUseCase.class,
    GetCurrentMeasurementSubjectStateUseCase.class,
    CorrectMeasurementSetUseCase.class,
    QueryMeasurementSetsUseCase.class,
    RetractMeasurementSetUseCase.class,
    FeedstockMeasurementCompatibilityFacade.class,
    ListFeedstockMeasurementsUseCase.class,
    QueryFeedstockPlanningBatchesUseCase.class,
    OptimizeFeedstockBatchCompositionUseCase.class,
    CreateProductionBatchUseCase.class,
    CreateProductionBatchCandidateUseCase.class,
    GetProductionBatchCandidateUseCase.class,
    GetProductionBatchUseCase.class,
    ListProductionBatchesUseCase.class,
    PotentialAbvPredictor.class,
    CalculateChemicalProfileCompositionUseCase.class,
    OptimizeChemicalProfileCompositionUseCase.class,
    JolicoeurSugarAcidityZoneGeometryProfile.class,
    SugarAcidityTargetResolver.class,
    AppleVarietyJsonSeedImporter.class
})
class OpenApiContractTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void openApiDescribesBearerJwtAuthentication() throws Exception {
    final var openApi = openApi();
    final var bearerAuth = openApi.path("components").path("securitySchemes").path("bearerAuth");

    assertThat(List.of(
        bearerAuth.path("type").asText(),
        bearerAuth.path("scheme").asText(),
        bearerAuth.path("bearerFormat").asText(),
        openApi.path("security").path(0).has("bearerAuth")
    )).containsExactly("http", "bearer", "JWT", true);
  }

  @Test
  void openApiDescribesEveryAccountDtoProperty() throws Exception {
    final var openApi = openApi();
    final var schemas = openApi.path("components").path("schemas");
    final var accountSchemaNames = List.of(
        "AccountMeResponse",
        "AccountMembershipResponse",
        "AccountPreferencesResponse",
        "AccountUserResponse",
        "AccountWorkspaceListResponse",
        "AccountWorkspaceMemberListResponse",
        "AccountWorkspaceMemberResponse",
        "AccountWorkspaceResponse",
        "UpdateAccountPreferencesRequest",
        "UpdateAccountProfileRequest"
    );
    final var descriptionViolations = accountSchemaNames.stream()
        .flatMap(schemaName -> accountDtoDescriptionViolations(schemas, schemaName))
        .sorted()
        .toList();

    assertThat(descriptionViolations)
        .as("account DTO schemas and properties must have semantic OpenAPI descriptions")
        .isEmpty();
  }

  @Test
  void openApiDescribesExplicitActiveWorkspaceContext() throws Exception {
    final var openApi = openApi();
    final var accountMeOperation = operation(openApi, "/account/me", "get");
    final var activeWorkspaceParameter = StreamSupport.stream(
            accountMeOperation.path("parameters").spliterator(),
            false
        )
        .filter(parameter -> ACTIVE_WORKSPACE_ID.equals(parameter.path("name").asText()))
        .findFirst()
        .orElseThrow();

    assertThat(List.of(
        activeWorkspaceParameter.path("in").asText(),
        activeWorkspaceParameter.path("required").asBoolean(false),
        activeWorkspaceParameter.path("schema").path("format").asText(),
        accountMeOperation.path("responses").has("400"),
        accountMeOperation.path("responses").has("422")
    )).containsExactly("header", false, "uuid", true, true);
  }

  @Test
  void openApiDescribesWorkspaceSpecificMeasurementFreshnessMetadata() throws Exception {
    final var openApi = openApi();
    final var metadataOperation = operation(openApi, "/measurement-units/metadata", "get");
    final var activeWorkspaceParameter = StreamSupport.stream(
            metadataOperation.path("parameters").spliterator(),
            false
        )
        .filter(parameter -> ACTIVE_WORKSPACE_ID.equals(parameter.path("name").asText()))
        .findFirst()
        .orElseThrow();

    assertThat(List.of(
        activeWorkspaceParameter.path("in").asText(),
        activeWorkspaceParameter.path("required").asBoolean(false),
        activeWorkspaceParameter.path("schema").path("format").asText(),
        metadataOperation.path("responses").has("401"),
        metadataOperation.path("responses").has("400"),
        metadataOperation.path("responses").has("403"),
        metadataOperation.path("responses").has("422")
    )).containsExactly("header", false, "uuid", true, true, true, true);
  }

  @Test
  void openApiMarksLegacyFeedstockMeasurementWritesAsDeprecated() throws Exception {
    final var openApi = openApi();

    assertThat(List.of(
        operation(openApi, "/feedstock-batches/{batchId}/measurements", "post")
            .path("deprecated")
            .asBoolean(),
        operation(openApi, "/feedstock-batches/{batchId}/measurements", "get")
            .path("deprecated")
            .asBoolean(),
        operation(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "put")
            .path("deprecated")
            .asBoolean(),
        operation(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "patch")
            .path("deprecated")
            .asBoolean(),
        operation(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "delete")
            .path("deprecated")
            .asBoolean()
    )).containsOnly(true);
  }

  @Test
  void openApiUsesLiteralDiscriminatorsForPolymorphicUnions() throws Exception {
    final var openApi = openApi();

    assertEnumPropertyValues(
        openApi,
        "DirectMeasurementCharacteristicRequest",
        "inputKind",
        "DIRECT"
    );
    assertEnumPropertyValues(
        openApi,
        "RepeatedMeasurementCharacteristicRequest",
        "inputKind",
        "REPEATED"
    );
    assertEnumPropertyValues(openApi, "PointMeasurementValueRequest", "kind", "POINT");
    assertEnumPropertyValues(openApi, "IntervalMeasurementValueRequest", "kind", "INTERVAL");
    assertEnumPropertyValues(openApi, "PointMeasurementValueResponse", "kind", "POINT");
    assertEnumPropertyValues(openApi, "IntervalMeasurementValueResponse", "kind", "INTERVAL");
    assertEnumPropertyValues(
        openApi,
        "PointProfileCharacteristicValueResponse",
        "valueKind",
        "POINT"
    );
    assertEnumPropertyValues(
        openApi,
        "IntervalProfileCharacteristicValueResponse",
        "valueKind",
        "INTERVAL_MIDPOINT"
    );
    assertEnumPropertyValues(
        openApi,
        "CalculatedProfileCharacteristicValueResponse",
        "valueKind",
        "CALCULATED"
    );
    assertEnumPropertyValues(openApi, "ExactChemicalMeasurementRequest", "kind", "EXACT");
    assertEnumPropertyValues(openApi, "RangeChemicalMeasurementRequest", "kind", "RANGE");
    assertEnumPropertyValues(openApi, "ExactChemicalMeasurementResponse", "kind", "EXACT");
    assertEnumPropertyValues(openApi, "RangeChemicalMeasurementResponse", "kind", "RANGE");
    assertEnumPropertyValues(
        openApi,
        "PredefinedZoneProfileTargetRequest",
        "kind",
        "PREDEFINED_ZONE"
    );
    assertEnumPropertyValues(
        openApi,
        "CustomRectangleProfileTargetRequest",
        "kind",
        "CUSTOM_RECTANGLE"
    );
    assertEnumPropertyValues(
        openApi,
        "RectangleSugarAcidityGraphZoneResponse",
        "shape",
        "RECTANGLE"
    );
    assertEnumPropertyValues(
        openApi,
        "EllipseSugarAcidityGraphZoneResponse",
        "shape",
        "ELLIPSE"
    );
  }

  @Test
  void openApiDescribesMeasurementCorrectionAndRetractionContracts() throws Exception {
    final var openApi = openApi();
    final var expectedVersion = operationParameter(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "delete",
        "expectedVersion"
    );
    final var expectedVersionSchema = resolveSchema(openApi, expectedVersion.path("schema"));

    assertThat(List.of(
        expectedVersion.path("required").asText(),
        expectedVersionSchema.path("type").asText(),
        expectedVersionSchema.path("format").asText(),
        expectedVersionSchema.path("minimum").asText()
    )).containsExactly("true", "integer", "int64", "0");

    assertResponseCodes(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "put",
        "200",
        "404",
        "409"
    );
    assertResponseCodes(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "patch",
        "200",
        "404",
        "409"
    );
    assertResponseCodes(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "delete",
        "204",
        "404",
        "409"
    );
  }

  @Test
  void openApiDescribesWorkspaceProfileSummaryAsCompositeEvidence() throws Exception {
    final var description = openApi()
        .path("components")
        .path("schemas")
        .path("FeedstockWorkspaceProfileSummaryResponse")
        .path("description")
        .asText();

    assertThat(description)
        .contains("independently selected", "different MeasurementSet", "profileEvidence")
        .doesNotContain("derived from the current measurement");
  }

  @Test
  void openApiDescribesJsonMediaTypesAndStrongDtoSchemas() throws Exception {
    final var openApi = openApi();

    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/classification", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/north-american", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/apple-varieties/{varietyId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/analysis/apple-varieties/profile-target-candidates", "post");
    assertJsonResponse(openApi, "/analysis/feedstock-batches/profile-target-candidates", "post");
    assertJsonResponse(openApi, "/chemical-profiles/sugar-acidity-zone/plot-metadata", "get");
    assertJsonResponse(openApi, "/measurement-units/metadata", "get");
    assertJsonResponse(openApi, "/measurement-sets", "post", "201");
    assertJsonResponse(openApi, "/measurement-sets", "get");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}", "get");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}", "put");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}", "patch");
    assertJsonResponse(openApi, "/material-subjects/{subjectType}/{subjectId}/current-state", "get");
    assertJsonResponse(openApi, "/chemical-profile-compositions/calculations", "post");
    assertJsonResponse(openApi, "/chemical-profile-compositions/optimizations", "post");
    assertJsonResponse(openApi, "/feedstock-batches", "get");
    assertJsonResponse(openApi, "/feedstock-batches", "post", "201");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "put");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "patch");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements", "post", "201");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "put");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "patch");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/feedstock-batches/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/fermentation-start-readiness", "get");
    assertJsonResponse(openApi, "/feedstock-batches/profile-target-scores", "post");
    assertJsonResponse(openApi, "/feedstock-batches/composition-optimizations", "post");
    assertJsonResponse(openApi, "/feedstock-workspace/batches", "post");
    assertJsonResponse(openApi, "/feedstock-measurements/{measurementId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/feedstock-measurements/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/feedstock-measurements/{measurementId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/production-batches", "get");
    assertJsonResponse(openApi, "/production-batches", "post", "201");
    assertJsonResponse(openApi, "/production-batches/{batchId}", "get");
    assertJsonResponse(openApi, "/production-batches/{batchId}/traceability", "get");
    assertJsonResponse(openApi, "/account/me", "get");
    assertJsonResponse(openApi, "/account/profile", "get");
    assertJsonResponse(openApi, "/account/profile", "patch");
    assertJsonResponse(openApi, "/account/preferences", "get");
    assertJsonResponse(openApi, "/account/preferences", "patch");
    assertJsonResponse(openApi, "/account/workspaces", "get");
    assertJsonResponse(openApi, "/account/workspaces/{workspaceId}/settings", "get");
    assertJsonResponse(openApi, "/account/workspaces/{workspaceId}/settings", "patch");
    assertJsonResponse(openApi, "/account/workspaces/{workspaceId}/members", "get");
    assertJsonResponse(openApi, "/account/workspace-invitations", "get");
    assertJsonResponse(openApi, "/account/workspace-invitations/{invitationId}", "get");

    assertJsonRequest(openApi, "/feedstock-batches", "post");
    assertJsonRequest(openApi, "/measurement-sets", "post");
    assertJsonRequest(openApi, "/measurement-sets/{measurementSetId}", "put");
    assertJsonRequest(openApi, "/measurement-sets/{measurementSetId}", "patch");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}", "put");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}", "patch");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements", "post");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "put");
    assertJsonRequest(openApi, "/feedstock-batches/{batchId}/measurements/{measurementId}", "patch");
    assertJsonRequest(openApi, "/account/workspaces/{workspaceId}/settings", "patch");
    assertJsonRequest(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/analysis/apple-varieties/profile-target-candidates", "post");
    assertJsonRequest(openApi, "/analysis/feedstock-batches/profile-target-candidates", "post");
    assertJsonRequest(openApi, "/chemical-profile-compositions/calculations", "post");
    assertJsonRequest(openApi, "/chemical-profile-compositions/optimizations", "post");
    assertJsonRequest(openApi, "/feedstock-batches/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/feedstock-batches/profile-target-scores", "post");
    assertJsonRequest(openApi, "/feedstock-batches/composition-optimizations", "post");
    assertJsonRequest(openApi, "/feedstock-workspace/batches", "post");
    assertJsonRequest(openApi, "/feedstock-measurements/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/production-batches", "post");
    assertJsonRequest(openApi, "/account/profile", "patch");
    assertJsonRequest(openApi, "/account/preferences", "patch");

    assertStringFormat(schemaProperty(openApi, "AppleVarietyClassificationResponse", "varietyId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockMeasurementResponse", "measuredAt"), "date-time");
    assertStringFormat(schemaProperty(openApi, "FeedstockSeasonResponse", "startsOn"), "date");
    assertArray(schemaProperty(openApi, "AppleVarietySugarAcidityZonePointBatchResponse", "points"));
    assertArray(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointBatchResponse", "points"));
    assertArray(schemaProperty(openApi, "FeedstockMeasurementSugarAcidityZonePointBatchResponse", "unplottable"));
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "currentMaterialStateId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "currentMeasurementId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "measuredAt"), "date-time");
    assertArray(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "missingAxes"));
    assertRequiredProperties(openApi, "FeedstockBatchGraphPointResponse",
        "batchId", "batchName", "workspaceId", "currentMaterialStateId", "currentMeasurementId", "measuredAt",
        "plottable", "missingAxes");
    assertNotRequiredProperties(openApi, "FeedstockBatchGraphPointResponse",
        "point", "zone", "northAmericanTanninLevel");
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "point"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "zone"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "northAmericanTanninLevel"));
    assertMissingProperties(
        openApi,
        "FeedstockBatchGraphPointResponse",
        "assessment",
        "capabilityAssessment"
    );
    assertRequiredProperties(
        openApi,
        "SugarAcidityGraphAssessmentResponse",
        "point"
    );
    assertNotRequiredProperties(
        openApi,
        "SugarAcidityGraphAssessmentResponse",
        "zone",
        "northAmericanTanninLevel"
    );
    assertMissingProperties(
        openApi,
        "SugarAcidityGraphAssessmentResponse",
        "sugar",
        "acidity",
        "balancedProfile"
    );
    assertNullable(schemaProperty(openApi, "SugarAcidityGraphAssessmentResponse", "zone"));
    assertNullable(
        schemaProperty(
            openApi,
            "SugarAcidityGraphAssessmentResponse",
            "northAmericanTanninLevel"
        )
    );
    assertMissingProperties(openApi, "SugarAcidityGraphAssessmentResponse", "zoneSystem");
    assertEnumProperty(
        openApi,
        "SugarAcidityGraphAssessmentResponse",
        "northAmericanTanninLevel",
        "LOW"
    );

    assertEnumProperty(openApi, "CreateFeedstockBatchRequest", "type", "JUICE");
    assertEnumPropertyValues(openApi, "CreateFeedstockBatchRequest", "type", "JUICE", "MUST", "BLEND", "UNKNOWN");
    assertEnumProperty(
        openApi,
        "ChemicalProfileCapabilityAssessmentResponse",
        "capability",
        "SUGAR_ACIDITY_GRAPH"
    );
    assertEnumProperty(openApi, "PotentialAbvPredictionResponse", "confidence", "LOW");
    assertNoLegacyPhysicalQuantityResponseFields(openApi);
    assertNoBareNumericResponsePropertiesOutsideAllowList(openApi);
    assertRequiredProperties(
        openApi,
        "PotentialAbvPredictionResponse",
        "originalGravity",
        "predictedFinalGravity",
        "predictedFinalGravitySource",
        "potentialAbv",
        "method",
        "confidence"
    );
    assertRequiredProperties(openApi, "DecimalQuantityResponse", "value", "unit");
    assertRequiredProperties(
        openApi,
        "AccountMeResponse",
        "user",
        "activeWorkspace",
        "memberships",
        "preferences",
        "personalCapabilities",
        "workspacePermissions"
    );
    assertRequiredProperties(
        openApi,
        "AccountWorkspaceResponse",
        "workspaceId",
        "name",
        "type"
    );
    assertStringFormat(schemaProperty(openApi, "AccountWorkspaceResponse", "workspaceId"), "uuid");
    assertRequiredProperties(openApi, "UpdateAccountProfileRequest", "displayName");
    assertNotRequiredProperties(openApi, "UpdateAccountProfileRequest", "avatarUrl");
    assertNullable(schemaProperty(openApi, "UpdateAccountProfileRequest", "avatarUrl"));
    assertRequiredProperties(openApi, "UpdateAccountPreferencesRequest", "locale", "timeZone");
    assertRequiredProperties(
        openApi,
        "WorkspaceSettingsResponse",
        "workspaceId",
        "measurementStaleAfter",
        "defaultAcidityBasis",
        "revision",
        "createdAt",
        "updatedAt"
    );
    assertStringFormat(schemaProperty(openApi, "WorkspaceSettingsResponse", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "WorkspaceSettingsResponse", "createdAt"), "date-time");
    assertStringFormat(schemaProperty(openApi, "WorkspaceSettingsResponse", "updatedAt"), "date-time");
    assertRequiredProperties(openApi, "UpdateWorkspaceSettingsRequest", "expectedRevision");
    assertNotRequiredProperties(
        openApi,
        "UpdateWorkspaceSettingsRequest",
        "measurementStaleAfter",
        "defaultAcidityBasis"
    );
    assertNullable(schemaProperty(openApi, "UpdateWorkspaceSettingsRequest", "measurementStaleAfter"));
    assertNullable(schemaProperty(openApi, "UpdateWorkspaceSettingsRequest", "defaultAcidityBasis"));

    assertRequiredProperties(
        openApi,
        "CreateFeedstockBatchRequest",
        "workspaceId",
        "name",
        "type",
        "season"
    );
    assertNotRequiredProperties(openApi, "CreateFeedstockBatchRequest", "vessel", "source", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "vessel"));
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "note"));
    assertExclusiveMinimumZero(schemaProperty(openApi, "CreateFeedstockBatchRequest", "availableVolumeLiters"));

    assertRequiredProperties(
        openApi,
        "CreateFeedstockMeasurementRequest",
        "name",
        "chemicalProfile",
        "measuredAt"
    );
    assertNotRequiredProperties(openApi, "CreateFeedstockMeasurementRequest", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockMeasurementRequest", "note"));

    assertRequiredProperties(
        openApi,
        "FeedstockMeasurementResponse",
        "measurementId",
        "workspaceId",
        "batchId",
        "name",
        "measuredAt",
        "chemicalProfile"
    );
    assertNotRequiredProperties(openApi, "FeedstockMeasurementResponse", "note");
    assertNullable(schemaProperty(openApi, "FeedstockMeasurementResponse", "note"));

    assertRequiredProperties(
        openApi,
        "SugarAcidityGraphPointResponse",
        "specificGravity",
        "titratableAcidity"
    );
    assertRequiredProperties(
        openApi,
        "ProfileTargetScoringCharacteristicsResponse",
        "specificGravity",
        "titratableAcidity"
    );
    assertNotRequiredProperties(openApi, "ProfileTargetScoringCharacteristicsResponse", "tannin");
    assertNullable(schemaProperty(openApi, "ProfileTargetScoringCharacteristicsResponse", "tannin"));
    assertRequiredProperties(
        openApi,
        "CalculatedCompositionCharacteristicsResponse",
        "specificGravity",
        "titratableAcidity"
    );
    assertNotRequiredProperties(
        openApi,
        "CalculatedCompositionCharacteristicsResponse",
        "tannin",
        "yeastAssimilableNitrogen"
    );
    assertNullable(schemaProperty(openApi, "CalculatedCompositionCharacteristicsResponse", "tannin"));
    assertNullable(
        schemaProperty(openApi, "CalculatedCompositionCharacteristicsResponse", "yeastAssimilableNitrogen")
    );

    assertRequiredProperties(
        openApi,
        "ProfileTargetDistanceResponse",
        "status",
        "fitStatus",
        "insideSugarAcidityTarget",
        "insideTarget",
        "missingAxes"
    );
    assertMissingProperties(
        openApi,
        "ProfileTargetDistanceResponse",
        "insideGraphTarget",
        "missingMeasurements"
    );
    assertRequiredProperties(
        openApi,
        "ProfileTargetCoordinatesResponse",
        "specificGravity",
        "titratableAcidity"
    );
    assertNotRequiredProperties(
        openApi,
        "ProfileTargetDistanceResponse",
        "value",
        "matchPercent",
        "tannin",
        "nearestTargetTannin"
    );
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "value"));
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "matchPercent"));
    assertEnumProperty(openApi, "ProfileTargetDistanceResponse", "fitStatus", "EXCELLENT");
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "ProfileTargetDistanceResponse", "nearestTargetTannin"));

    assertRequiredProperties(openApi, "SugarAcidityGraphRangeResponse", "min", "max");
    assertRequiredProperties(
        openApi,
        "SugarAcidityGraphAxisResponse",
        "unit",
        "min",
        "max",
        "boundaryValues"
    );
    assertMissingProperties(
        openApi,
        "SugarAcidityGraphAxisResponse",
        "code",
        "label",
        "thresholdLines"
    );
    assertRequiredProperties(openApi, "MeasurementUnitsMetadataResponse", "units", "measurementFreshness");
    assertRequiredProperties(openApi, "MeasurementFreshnessMetadataResponse", "staleAfter");
    assertRequiredProperties(
        openApi,
        "MeasurementUnitMetadataResponse",
        "code",
        "canonicalUnit",
        "shortLabel",
        "fullLabel",
        "min",
        "max",
        "step"
    );
    assertEnumProperty(openApi, "MeasurementUnitMetadataResponse", "code", "SG");
    assertRequiredProperties(
        openApi,
        "EllipseSugarAcidityGraphZoneResponse",
        "zone",
        "specificGravityCenter",
        "titratableAcidityCenter",
        "specificGravityRadius",
        "titratableAcidityRadius"
    );
    assertRequiredProperties(
        openApi,
        "SugarAcidityGraphMetadataResponse",
        "specificGravityAxis",
        "titratableAcidityAxis",
        "zones"
    );
    assertMissingProperties(openApi, "SugarAcidityGraphMetadataResponse", "zoneSystem");
    assertRequiredProperties(
        openApi,
        "CustomRectangleProfileTargetRequest",
        "kind",
        "specificGravityMin",
        "specificGravityMax",
        "titratableAcidityMin",
        "titratableAcidityMax"
    );

    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest",
        "workspaceId"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest",
        "batchId",
        "season",
        "after",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "batchId"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "season"));

    assertParameterRequired(openApi, "/feedstock-batches", "get", "workspaceId", true);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonStartsOn", false);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonEndsOn", false);

    assertOneOfSchema(openApi, "ChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "ExactChemicalMeasurementRequest");
    assertNoAllOfSchema(openApi, "RangeChemicalMeasurementRequest");
    assertOneOfSchema(openApi, "ChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "ExactChemicalMeasurementResponse");
    assertNoAllOfSchema(openApi, "RangeChemicalMeasurementResponse");
    assertOneOfSchema(openApi, "MeasurementCharacteristicRequest");
    assertNoAllOfSchema(openApi, "DirectMeasurementCharacteristicRequest");
    assertNoAllOfSchema(openApi, "RepeatedMeasurementCharacteristicRequest");
    assertOneOfSchema(openApi, "MeasurementValueRequest");
    assertNoAllOfSchema(openApi, "PointMeasurementValueRequest");
    assertNoAllOfSchema(openApi, "IntervalMeasurementValueRequest");
    assertOneOfSchema(openApi, "MeasurementValueResponse");
    assertNoAllOfSchema(openApi, "PointMeasurementValueResponse");
    assertNoAllOfSchema(openApi, "IntervalMeasurementValueResponse");
    assertRequiredProperties(
        openApi,
        "DirectMeasurementCharacteristicRequest",
        "inputKind",
        "characteristic",
        "value",
        "measuredAt",
        "provenance"
    );
    assertRequiredProperties(
        openApi,
        "RepeatedMeasurementCharacteristicRequest",
        "inputKind",
        "characteristic",
        "observations",
        "derivationMethod"
    );
    assertRequiredProperties(
        openApi,
        "CreateMeasurementSetRequest",
        "subject",
        "observedMaterialStateId",
        "name",
        "measurements"
    );
    assertStringFormat(schemaProperty(openApi, "CreateMeasurementSetRequest", "observedMaterialStateId"), "uuid");
    assertRequiredProperties(openApi, "PointMeasurementValueRequest", "kind", "value", "unit");
    assertRequiredProperties(
        openApi,
        "IntervalMeasurementValueRequest",
        "kind",
        "lower",
        "upper",
        "unit"
    );
    assertRequiredProperties(
        openApi,
        "PointMeasurementValueResponse",
        "kind",
        "value"
    );
    assertRequiredProperties(
        openApi,
        "IntervalMeasurementValueResponse",
        "kind",
        "lower",
        "upper"
    );
    assertRequiredProperties(
        openApi,
        "MeasurementSetResponse",
        "measurementSetId",
        "workspaceId",
        "subject",
        "lifecycle",
        "version",
        "revisionId",
        "revisionNumber",
        "materialStateId",
        "name",
        "revisionCreatedAt",
        "actor",
        "records",
        "createdAt",
        "updatedAt"
    );
    assertStringFormat(schemaProperty(openApi, "MeasurementSetResponse", "materialStateId"), "uuid");
    assertRequiredProperties(
        openApi,
        "MeasurementActorReferenceResponse",
        "type",
        "id"
    );
    assertNotRequiredProperties(
        openApi,
        "MeasurementSetResponse",
        "note",
        "correctionReason"
    );
    assertRequiredProperties(
        openApi,
        "MeasurementEvidenceResponse",
        "characteristic",
        "origin",
        "materialStateId",
        "measurementSetId",
        "recordId",
        "measuredAt",
        "freshnessStatus",
        "provenance",
        "validity"
    );
    assertStringFormat(schemaProperty(openApi, "MeasurementEvidenceResponse", "materialStateId"), "uuid");
    assertRequiredProperties(
        openApi,
        "CurrentMaterialStateResponse",
        "subject",
        "currentMaterialStateId",
        "characteristics"
    );
    assertStringFormat(schemaProperty(openApi, "CurrentMaterialStateResponse", "currentMaterialStateId"), "uuid");
    assertArray(schemaProperty(openApi, "CurrentMaterialStateResponse", "characteristics"));
    assertRequiredProperties(
        openApi,
        "CurrentMaterialCharacteristicResponse",
        "characteristic",
        "value",
        "origin",
        "evidenceMaterialStateId",
        "measurementSetId",
        "recordId",
        "measuredAt",
        "provenance",
        "freshnessStatus",
        "validity"
    );
    assertStringFormat(
        schemaProperty(openApi, "CurrentMaterialCharacteristicResponse", "evidenceMaterialStateId"),
        "uuid"
    );
    assertRequiredProperties(openApi, "ExactChemicalMeasurementResponse", "kind", "value");
    assertRequiredProperties(openApi, "RangeChemicalMeasurementResponse", "kind", "min", "max");
    assertNotRequiredProperties(openApi, "ExactChemicalMeasurementResponse", "unit");
    assertNotRequiredProperties(openApi, "RangeChemicalMeasurementResponse", "unit");
    assertNotRequiredProperties(
        openApi,
        "ChemicalProfileResponse",
        "specificGravity",
        "ph",
        "titratableAcidity",
        "tannin",
        "yeastAssimilableNitrogen"
    );
    assertNullable(schemaProperty(openApi, "ChemicalProfileResponse", "specificGravity"));
    assertMissingProperties(openApi, "ChemicalProfileResponse", "sugar");
    assertOneOfSchema(openApi, "ProfileTargetRequest");
    assertNoAllOfSchema(openApi, "PredefinedZoneProfileTargetRequest");
    assertNoAllOfSchema(openApi, "CustomRectangleProfileTargetRequest");
    assertOneOfSchema(openApi, "ProfileCharacteristicValueResponse");
    assertNoAllOfSchema(openApi, "PointProfileCharacteristicValueResponse");
    assertNoAllOfSchema(openApi, "IntervalProfileCharacteristicValueResponse");
    assertNoAllOfSchema(openApi, "CalculatedProfileCharacteristicValueResponse");
    assertOneOfSchema(openApi, "SugarAcidityGraphZoneResponse");
    assertNoAllOfSchema(openApi, "RectangleSugarAcidityGraphZoneResponse");
    assertNoAllOfSchema(openApi, "EllipseSugarAcidityGraphZoneResponse");

    assertRequiredProperties(
        openApi,
        "ChemicalProfileCompositionResponse",
        "sources",
        "characteristics",
        "warnings"
    );
    assertNotRequiredProperties(
        openApi,
        "ChemicalProfileCompositionResponse",
        "potentialAbv",
        "totalVolume",
        "normalizerCode",
        "targetDistance"
    );
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionResponse", "totalVolume"));
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionResponse", "potentialAbv"));
    assertRequiredProperties(openApi, "ChemicalProfileCompositionSourceResponse", "reference");
    assertNotRequiredProperties(openApi, "ChemicalProfileCompositionSourceResponse", "requestedVolume", "proportion");
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionSourceResponse", "requestedVolume"));
    assertRequiredProperties(
        openApi,
        "ChemicalProfileCompositionOptimizationCandidateResponse",
        "sources",
        "characteristics",
        "warnings",
        "targetDistance",
        "objectiveAssessment"
    );
    assertNotRequiredProperties(
        openApi,
        "ChemicalProfileCompositionOptimizationCandidateResponse",
        "potentialAbv",
        "totalVolume"
    );
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionOptimizationCandidateResponse", "totalVolume"));
    assertNullable(schemaProperty(openApi, "ChemicalProfileCompositionOptimizationCandidateResponse", "potentialAbv"));
    assertNotRequiredProperties(openApi, "AppleVarietySugarAcidityZoneResponse", "potentialAbv");
    assertNullable(schemaProperty(openApi, "AppleVarietySugarAcidityZoneResponse", "potentialAbv"));
    assertMissingProperties(openApi, "CalculateChemicalProfileCompositionRequest", "scoringPurpose");
    assertMissingProperties(openApi, "OptimizeChemicalProfileCompositionRequest", "scoringPurpose");
    assertMissingProperties(openApi, "OptimizeFeedstockBatchCompositionRequest", "purpose");
    assertNoSchemaComposesDiscriminatedUnionThroughAllOf(openApi);
  }

  @Test
  void openApiDescribesAnalysisProfileTargetRequiredNullableContracts() throws Exception {
    final var openApi = openApi();

    assertRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidateSearchRequest",
        "analysisScenario",
        "target"
    );
    assertMissingProperties(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "purpose");
    assertNotRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidateSearchRequest",
        "nameContains",
        "zone",
        "capability",
        "capable",
        "tanninTarget",
        "weights",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "nameContains"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "zone"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "capability"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "capable"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "tanninTarget"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "weights"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "limit"));

    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateSearchRequest",
        "workspaceId",
        "analysisScenario",
        "target"
    );
    assertMissingProperties(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "purpose");
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateSearchRequest",
        "batchIds",
        "season",
        "zone",
        "capability",
        "capable",
        "tanninTarget",
        "weights",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "batchIds"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "season"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "zone"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "capability"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "capable"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "tanninTarget"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "weights"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "limit"));

    assertRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidatesResponse",
        "analysisScenario",
        "normalizerCode",
        "candidates"
    );
    assertArray(schemaProperty(openApi, "AppleVarietyProfileTargetCandidatesResponse", "candidates"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidatesResponse",
        "analysisScenario",
        "normalizerCode",
        "candidates"
    );
    assertArray(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidatesResponse", "candidates"));
    assertRequiredProperties(
        openApi,
        "ScoreFeedstockBatchProfileTargetRequest",
        "workspaceId",
        "analysisScenario",
        "target"
    );
    assertMissingProperties(openApi, "ScoreFeedstockBatchProfileTargetRequest", "purpose");
    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetScoreBatchResponse",
        "analysisScenario",
        "normalizerCode",
        "scores"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetScoreResponse",
        "batchId",
        "workspaceId",
        "currentMaterialStateId",
        "currentMeasurementId",
        "currentMeasurementName",
        "characteristics",
        "distance"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockBatchProfileTargetScoreResponse", "currentMaterialStateId"),
        "uuid"
    );
    assertEnumPropertyValues(
        openApi,
        "ScoreFeedstockBatchProfileTargetRequest",
        "analysisScenario",
        "GRAPH_EXPLORATION",
        "BLENDING_PLANNING",
        "TARGET_ZONE_MATCHING"
    );
    assertMissingSchema(openApi, "ProfileTargetScoringPurpose");

    assertRequiredProperties(
        openApi,
        "AppleVarietyProfileTargetCandidateResponse",
        "varietyId",
        "varietyName",
        "characteristics",
        "distance",
        "contributionRole"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateResponse", "currentMaterialStateId"),
        "uuid"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateResponse",
        "batchId",
        "currentMaterialStateId",
        "currentMeasurementId",
        "currentMeasurementName",
        "characteristics",
        "distance",
        "contributionRole"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockWorkspaceTargetCandidateResponse", "currentMaterialStateId"),
        "uuid"
    );
    assertRequiredProperties(openApi, "CompositionContributionEvidenceResponse",
        "axis", "sourceValue", "targetCoordinate", "deviationFromTarget");
    assertRequiredProperties(openApi, "CompositionContributionRoleAssessmentResponse",
        "roles", "evidence", "missingAxes");
    assertMissingProperties(openApi, "CompositionContributionRoleAssessmentResponse", "assessable");
    assertArray(schemaProperty(openApi, "CompositionContributionRoleAssessmentResponse", "roles"));
    assertArray(schemaProperty(openApi, "CompositionContributionRoleAssessmentResponse", "evidence"));
    assertArray(schemaProperty(openApi, "CompositionContributionRoleAssessmentResponse", "missingAxes"));
  }

  @Test
  void openApiDescribesProductionAndFeedstockRequiredNullableContracts() throws Exception {
    final var openApi = openApi();

    assertParameterRequired(openApi, "/production-batches", "get", "workspaceId", true);
    assertRequiredProperties(
        openApi,
        "CreateProductionBatchRequest",
        "workspaceId",
        "name",
        "vessel",
        "candidateId"
    );
    assertStringFormat(schemaProperty(openApi, "CreateProductionBatchRequest", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "CreateProductionBatchRequest", "candidateId"), "uuid");
    assertNotRequiredProperties(openApi, "CreateProductionBatchRequest", "note");
    assertNullable(schemaProperty(openApi, "CreateProductionBatchRequest", "note"));

    assertRequiredProperties(openApi, "CreateProductionBatchCandidateRequest", "workspaceId", "sources");
    assertStringFormat(schemaProperty(openApi, "CreateProductionBatchCandidateRequest", "workspaceId"), "uuid");
    assertMissingProperties(openApi, "CreateProductionBatchCandidateRequest", "scoringPurpose");
    assertNotRequiredProperties(
        openApi,
        "CreateProductionBatchCandidateRequest",
        "tanninPolicy",
        "target",
        "tanninTarget",
        "weights",
        "calculator",
        "normalizer",
        "objectiveFunction"
    );
    assertRequiredProperties(
        openApi,
        "ProductionBatchCandidateSourceRequest",
        "feedstockBatchId",
        "volumeLiters"
    );

    assertRequiredProperties(
        openApi,
        "ProductionBatchDetailsResponse",
        "batchId",
        "workspaceId",
        "currentMaterialStateId",
        "name",
        "vessel",
        "lifecycle",
        "alcoholicFermentationState",
        "currentVolume",
        "initialProfileSnapshot",
        "potentialAbv"
    );
    assertStringFormat(schemaProperty(openApi, "ProductionBatchDetailsResponse", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "ProductionBatchDetailsResponse", "currentMaterialStateId"), "uuid");
    assertNotRequiredProperties(
        openApi,
        "ProductionBatchDetailsResponse",
        "yeastAssimilableNitrogen",
        "note"
    );
    assertNullable(schemaProperty(openApi, "ProductionBatchDetailsResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "ProductionBatchDetailsResponse", "note"));

    assertRequiredProperties(
        openApi,
        "ProductionBatchResponse",
        "batchId",
        "workspaceId",
        "name",
        "vessel",
        "lifecycle",
        "alcoholicFermentationState",
        "candidateId",
        "composition"
    );
    assertStringFormat(schemaProperty(openApi, "ProductionBatchResponse", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "ProductionBatchResponse", "candidateId"), "uuid");
    assertNotRequiredProperties(openApi, "ProductionBatchResponse", "note");
    assertNullable(schemaProperty(openApi, "ProductionBatchResponse", "note"));
    assertRequiredProperties(
        openApi,
        "ProductionBatchCompositionResponse",
        "sources",
        "characteristics",
        "totalVolume",
        "warnings"
    );
    assertNotRequiredProperties(
        openApi,
        "ProductionBatchCompositionResponse",
        "potentialAbv",
        "normalizerCode",
        "targetDistance"
    );
    assertNullable(schemaProperty(openApi, "ProductionBatchCompositionResponse", "potentialAbv"));
    assertNullable(schemaProperty(openApi, "ProductionBatchCompositionResponse", "normalizerCode"));
    assertNullable(schemaProperty(openApi, "ProductionBatchCompositionResponse", "targetDistance"));
    assertRequiredProperties(
        openApi,
        "ProductionBatchCandidateResponse",
        "candidateId",
        "workspaceId",
        "status",
        "composition"
    );
    assertStringFormat(schemaProperty(openApi, "ProductionBatchCandidateResponse", "candidateId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "ProductionBatchCandidateResponse", "workspaceId"), "uuid");
    assertNotRequiredProperties(openApi, "ProductionBatchCandidateResponse", "confirmedProductionBatchId");
    assertNullable(schemaProperty(openApi, "ProductionBatchCandidateResponse", "confirmedProductionBatchId"));
    assertRequiredProperties(openApi, "ProductionBatchSourceResponse", "feedstockBatchId", "volume");

    assertRequiredProperties(openApi, "BatchTraceabilityGraphResponse", "nodes", "edges");
    assertRequiredProperties(openApi, "BatchTraceNodeResponse", "traceStateId", "subject");
    assertStringFormat(schemaProperty(openApi, "BatchTraceNodeResponse", "traceStateId"), "uuid");
    assertNotRequiredProperties(openApi, "BatchTraceNodeResponse", "snapshot");
    assertNullable(schemaProperty(openApi, "BatchTraceNodeResponse", "snapshot"));
    assertRequiredProperties(
        openApi,
        "BatchTraceEdgeResponse",
        "stepId",
        "stepType",
        "occurredAt",
        "inputs",
        "outputs"
    );
    assertStringFormat(schemaProperty(openApi, "BatchTraceEdgeResponse", "occurredAt"), "date-time");
    assertEnumPropertyValues(
        openApi,
        "BatchTraceEdgeResponse",
        "stepType",
        "FEEDSTOCK_BATCH_CREATED",
        "PRODUCTION_BATCH_CREATED"
    );
    assertRequiredProperties(openApi, "BatchTraceStepSubjectResponse", "traceStateId", "subject");
    assertStringFormat(schemaProperty(openApi, "BatchTraceStepSubjectResponse", "traceStateId"), "uuid");
    assertNotRequiredProperties(openApi, "BatchTraceStepSubjectResponse", "snapshot");
    assertNullable(schemaProperty(openApi, "BatchTraceStepSubjectResponse", "snapshot"));
    assertRequiredProperties(openApi, "BatchTraceSubjectResponse", "type", "id");
    assertStringFormat(schemaProperty(openApi, "BatchTraceSubjectResponse", "id"), "uuid");
    assertNotRequiredProperties(openApi, "BatchTraceSubjectResponse", "displayNameSnapshot");
    assertNullable(schemaProperty(openApi, "BatchTraceSubjectResponse", "displayNameSnapshot"));
    assertEnumPropertyValues(
        openApi,
        "BatchTraceSubjectResponse",
        "type",
        "FEEDSTOCK_BATCH",
        "PRODUCTION_BATCH"
    );
    assertRequiredProperties(openApi, "BatchTraceMaterialSnapshotResponse", "axes");
    assertNotRequiredProperties(openApi, "BatchTraceMaterialSnapshotResponse", "volume", "confidence");
    assertNullable(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "volume"));
    assertNullable(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "confidence"));
    assertRequiredProperties(openApi, "BatchTraceAxisValueResponse", "axis", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "BatchTraceAxisValueResponse",
        "min",
        "max"
    );
    assertNullable(schemaProperty(openApi, "BatchTraceAxisValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "BatchTraceAxisValueResponse", "max"));

    assertRequiredProperties(
        openApi,
        "UpdateFeedstockBatchRequest",
        "workspaceId",
        "name",
        "type",
        "season"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchResponse",
        "batchId",
        "workspaceId",
        "name",
        "type",
        "season",
        "availableVolume"
    );
    assertNotRequiredProperties(openApi, "UpdateFeedstockBatchRequest", "source", "note");
    assertNotRequiredProperties(openApi, "FeedstockBatchResponse", "vessel", "source", "note");
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "note"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchResponse", "vessel"));
    assertMissingProperties(openApi, "UpdateFeedstockBatchRequest", "availableVolumeLiters", "vessel");
    assertRequiredProperties(openApi, "PatchFeedstockBatchRequest", "workspaceId");
    assertNotRequiredProperties(
        openApi,
        "PatchFeedstockBatchRequest",
        "name",
        "type",
        "season",
        "source",
        "note"
    );
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "name"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "season"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "note"));
    assertMissingProperties(openApi, "PatchFeedstockBatchRequest", "availableVolumeLiters", "vessel");

    assertRequiredProperties(openApi, "UpdateFeedstockMeasurementRequest", "name", "chemicalProfile", "measuredAt");
    assertNotRequiredProperties(openApi, "UpdateFeedstockMeasurementRequest", "note");
    assertNullable(schemaProperty(openApi, "UpdateFeedstockMeasurementRequest", "note"));
    assertNotRequiredProperties(
        openApi,
        "PatchFeedstockMeasurementRequest",
        "name",
        "chemicalProfile",
        "measuredAt",
        "note"
    );
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "name"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "chemicalProfile"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "measuredAt"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockMeasurementRequest", "note"));

    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentPotentialAbvResponse",
        "batchId",
        "workspaceId",
        "currentMaterialStateId",
        "currentMeasurementId",
        "currentMeasurementName",
        "originalGravityValueKind",
        "prediction"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockBatchCurrentPotentialAbvResponse", "currentMaterialStateId"),
        "uuid"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentFermentationStartReadinessResponse",
        "batchId",
        "workspaceId",
        "currentMaterialStateId",
        "currentMeasurementId",
        "currentMeasurementName",
        "assessment"
    );
    assertStringFormat(
        schemaProperty(
            openApi,
            "FeedstockBatchCurrentFermentationStartReadinessResponse",
            "currentMaterialStateId"
        ),
        "uuid"
    );
    assertNotRequiredProperties(
        openApi,
        "FermentationStartReadinessCheckResponse",
        "value",
        "unit",
        "managedByAction"
    );
    assertNullable(schemaProperty(openApi, "FermentationStartReadinessCheckResponse", "value"));
    assertNullable(schemaProperty(openApi, "FermentationStartReadinessCheckResponse", "managedByAction"));
    assertEnumProperty(
        openApi,
        "FermentationStartReadinessCheckResponse",
        "code",
        "PH_BELOW_GENERIC_YEAST_TOLERANCE"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchCompositionOptimizationCandidateResponse",
        "sources",
        "characteristics",
        "warnings",
        "targetDistance",
        "objectiveAssessment"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCompositionOptimizationCandidateResponse",
        "totalVolume"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCompositionOptimizationCandidateResponse", "totalVolume"));
    assertRequiredProperties(openApi, "FeedstockBatchCompositionOptimizationSourceResponse", "batchId");
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCompositionOptimizationSourceResponse",
        "requestedVolume",
        "proportion"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCompositionOptimizationSourceResponse", "requestedVolume"));
  }

  @Test
  void openApiDescribesFeedstockWorkspaceRequiredNullableContracts() throws Exception {
    final var openApi = openApi();

    assertRequiredProperties(openApi, "FeedstockWorkspaceBatchQueryRequest", "workspaceId");
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchQueryRequest",
        "season",
        "target",
        "tanninTarget",
        "weights",
        "analysisScenario",
        "targetCandidateLimit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "season"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "target"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "tanninTarget"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "weights"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "analysisScenario"));
    assertMissingProperties(openApi, "FeedstockWorkspaceBatchQueryRequest", "purpose");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchQueryRequest", "targetCandidateLimit"));

    assertRequiredProperties(openApi, "FeedstockWorkspaceResponse", "batches");
    assertArray(schemaProperty(openApi, "FeedstockWorkspaceResponse", "batches"));
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceResponse", "targetAnalysis");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceResponse", "targetAnalysis"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchResponse",
        "batch",
        "currentMaterialStateId",
        "planningReadiness",
        "dataQuality"
    );
    assertStringFormat(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "currentMaterialStateId"), "uuid");
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchResponse",
        "currentMeasurement",
        "profileSummary",
        "measurementFreshness",
        "sugarAcidity",
        "potentialAbv",
        "yeastAssimilableNitrogen",
        "fermentationStartReadiness",
        "targetCandidate"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "currentMeasurement"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "profileSummary"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "measurementFreshness"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "sugarAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "potentialAbv"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "fermentationStartReadiness"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "targetCandidate"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceCurrentMeasurementResponse",
        "measurementId",
        "name",
        "measuredAt",
        "chemicalProfile"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "measurementId"),
        "uuid"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "measuredAt"),
        "date-time"
    );
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "note");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "note"));

    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceProfileSummaryResponse",
        "specificGravity",
        "titratableAcidity",
        "ph",
        "tannin",
        "yan"
    );
    assertNullable(
        schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "specificGravity")
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "titratableAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "ph"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileSummaryResponse", "yan"));

    assertRequiredProperties(openApi, "FeedstockWorkspaceProfileValueResponse", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceProfileValueResponse",
        "min",
        "max"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceProfileValueResponse", "max"));

    assertRequiredProperties(openApi, "FeedstockWorkspaceMeasurementFreshnessResponse", "status", "stale");
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceMeasurementFreshnessResponse", "reason");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceMeasurementFreshnessResponse", "reason"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceSugarAcidityResponse",
        "plottable",
        "capabilityAssessment"
    );
    assertNotRequiredProperties(openApi, "FeedstockWorkspaceSugarAcidityResponse", "point", "assessment");
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceSugarAcidityResponse", "point"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceSugarAcidityResponse", "assessment"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspacePotentialAbvResponse",
        "originalGravityValueKind",
        "prediction"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceTargetAnalysisResponse",
        "analysisScenario",
        "normalizerCode"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceTargetCandidateResponse",
        "currentMaterialStateId",
        "characteristics",
        "distance",
        "contributionRole"
    );
    assertRequiredProperties(
        openApi,
        "CompositionContributionEvidenceResponse",
        "axis",
        "sourceValue",
        "targetCoordinate",
        "deviationFromTarget",
        "direction",
        "effect",
        "severity"
    );
    assertEnumProperty(
        openApi,
        "CompositionContributionEvidenceResponse",
        "direction",
        "ABOVE_TARGET"
    );
    assertEnumProperty(
        openApi,
        "CompositionContributionEvidenceResponse",
        "effect",
        "RAISES_SUGAR_POTENTIAL"
    );
    assertEnumProperty(
        openApi,
        "CompositionContributionEvidenceResponse",
        "severity",
        "SUPPORTING"
    );
    assertRequiredProperties(openApi, "FeedstockWorkspacePlanningReadinessResponse", "status", "checks");
    assertArray(schemaProperty(openApi, "FeedstockWorkspacePlanningReadinessResponse", "checks"));
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspacePlanningReadinessCheckResponse",
        "factor",
        "severity",
        "code",
        "message"
    );
    assertEnumProperty(
        openApi,
        "FeedstockWorkspacePlanningReadinessCheckResponse",
        "code",
        "CURRENT_PROFILE_PRESENT"
    );
    assertRequiredProperties(openApi, "FeedstockWorkspaceDataQualityResponse", "status", "indicators");
    assertArray(schemaProperty(openApi, "FeedstockWorkspaceDataQualityResponse", "indicators"));
    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceDataQualityIndicatorResponse",
        "code",
        "severity",
        "message"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchDetailResponse",
        "batch",
        "currentMaterialStateId",
        "currentProfile",
        "measurements"
    );
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchDetailResponse", "currentMaterialStateId"), "uuid");
    assertArray(schemaProperty(openApi, "FeedstockBatchDetailResponse", "measurements"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentProfileResponse",
        "planningReadiness",
        "dataQuality"
    );
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCurrentProfileResponse",
        "latestMeasurement",
        "profileSummary",
        "sugarAcidityAssessment",
        "potentialAbv",
        "yeastAssimilableNitrogen",
        "measurementFreshness",
        "fermentationStartReadiness",
        "lastOperation"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "latestMeasurement"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "profileSummary"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "sugarAcidityAssessment"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "potentialAbv"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "yeastAssimilableNitrogen"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "measurementFreshness"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "fermentationStartReadiness"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "lastOperation"));

    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchDetailProfileSummaryResponse",
        "specificGravity",
        "titratableAcidity",
        "ph",
        "tannin",
        "yan"
    );
    assertNullable(
        schemaProperty(openApi, "FeedstockBatchDetailProfileSummaryResponse", "specificGravity")
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailProfileSummaryResponse", "titratableAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailProfileSummaryResponse", "ph"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailProfileSummaryResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailProfileSummaryResponse", "yan"));

    assertRequiredProperties(openApi, "FeedstockBatchDetailProfileValueResponse", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchDetailProfileValueResponse",
        "min",
        "max"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailProfileValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailProfileValueResponse", "max"));

    assertRequiredProperties(openApi, "FeedstockBatchDetailMeasurementFreshnessResponse", "status", "stale");
    assertNotRequiredProperties(openApi, "FeedstockBatchDetailMeasurementFreshnessResponse", "reason");
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailMeasurementFreshnessResponse", "reason"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchDetailSugarAcidityResponse",
        "plottable",
        "capabilityAssessment"
    );
    assertNotRequiredProperties(openApi, "FeedstockBatchDetailSugarAcidityResponse", "point", "assessment");
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailSugarAcidityResponse", "point"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchDetailSugarAcidityResponse", "assessment"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchDetailPotentialAbvResponse",
        "originalGravityValueKind",
        "prediction"
    );
    assertRequiredProperties(openApi, "FeedstockBatchDetailPlanningReadinessResponse", "status", "checks");
    assertArray(schemaProperty(openApi, "FeedstockBatchDetailPlanningReadinessResponse", "checks"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchDetailPlanningReadinessCheckResponse",
        "factor",
        "severity",
        "code",
        "message"
    );
    assertEnumProperty(
        openApi,
        "FeedstockBatchDetailPlanningReadinessCheckResponse",
        "code",
        "TITRATABLE_ACIDITY_MISSING"
    );
    assertRequiredProperties(openApi, "FeedstockBatchDetailDataQualityResponse", "status", "indicators");
    assertArray(schemaProperty(openApi, "FeedstockBatchDetailDataQualityResponse", "indicators"));
    assertRequiredProperties(
        openApi,
        "FeedstockBatchDetailDataQualityIndicatorResponse",
        "code",
        "severity",
        "message"
    );
    assertRequiredProperties(openApi, "FeedstockBatchDetailLastOperationResponse", "type", "occurredAt");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchDetailLastOperationResponse", "occurredAt"), "date-time");
    assertEnumSchema(openApi, "FeedstockCurrentProfileSignalSeverity", "OK", "INFO", "WARNING", "BLOCKING");
    assertMissingSchema(openApi, "FeedstockPlanningSignalSeverity");
    assertMissingSchema(openApi, "FeedstockPlanningReadinessSeverity");
    assertMissingSchema(openApi, "FeedstockPlanningDataQualitySeverity");
    assertPropertyReferencesSchema(
        openApi,
        "FeedstockWorkspacePlanningReadinessCheckResponse",
        "severity",
        "FeedstockCurrentProfileSignalSeverity"
    );
    assertPropertyReferencesSchema(
        openApi,
        "FeedstockWorkspaceDataQualityIndicatorResponse",
        "severity",
        "FeedstockCurrentProfileSignalSeverity"
    );
  }

  private JsonNode openApi() throws Exception {
    final var response = mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    assertThat(response.getContentType())
        .isNotNull()
        .satisfies(contentType -> assertThat(
            MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_JSON)
        ).isTrue());

    return objectMapper.readTree(response.getContentAsString());
  }

  private static void assertJsonResponse(
      JsonNode openApi,
      String path,
      String method
  ) {
    assertJsonResponse(openApi, path, method, "200");
  }

  private static void assertJsonResponse(
      JsonNode openApi,
      String path,
      String method,
      String status
  ) {
    final var content = operation(openApi, path, method)
        .path("responses")
        .path(status)
        .path("content");

    assertThat(content.has("application/json"))
        .as("%s %s response content type", method, path)
        .isTrue();
    assertThat(content.has("*/*"))
        .as("%s %s wildcard response content type", method, path)
        .isFalse();
  }

  private static void assertJsonRequest(
      JsonNode openApi,
      String path,
      String method
  ) {
    final var content = operation(openApi, path, method)
        .path("requestBody")
        .path("content");

    assertThat(content.has("application/json"))
        .as("%s %s request content type", method, path)
        .isTrue();
    assertThat(content.has("*/*"))
        .as("%s %s wildcard request content type", method, path)
        .isFalse();
  }

  private static JsonNode operation(
      JsonNode openApi,
      String path,
      String method
  ) {
    final var operation = openApi.path("paths").path(path).path(method);
    assertThat(operation.isMissingNode())
        .as("%s %s operation exists", method, path)
        .isFalse();
    return operation;
  }

  private static JsonNode operationParameter(
      JsonNode openApi,
      String path,
      String method,
      String parameterName
  ) {
    return StreamSupport.stream(operation(openApi, path, method)
            .path("parameters")
            .spliterator(), false)
        .filter(parameter -> parameterName.equals(parameter.path("name").asText()))
        .findFirst()
        .orElseThrow();
  }

  private static JsonNode schemaProperty(
      JsonNode openApi,
      String schemaName,
      String propertyName
  ) {
    final var property = openApi
        .path("components")
        .path("schemas")
        .path(schemaName)
        .path("properties")
        .path(propertyName);

    assertThat(property.isMissingNode())
        .as("%s.%s exists", schemaName, propertyName)
        .isFalse();
    return property;
  }

  private static void assertMissingSchema(
      JsonNode openApi,
      String schemaName
  ) {
    final var schema = openApi
        .path("components")
        .path("schemas")
        .path(schemaName);

    assertThat(schema.isMissingNode())
        .as("%s schema should not be generated", schemaName)
        .isTrue();
  }

  private static void assertPropertyReferencesSchema(
      JsonNode openApi,
      String schemaName,
      String propertyName,
      String expectedSchemaName
  ) {
    final var property = schemaProperty(openApi, schemaName, propertyName);

    assertThat(referencedSchemaName(property))
        .as("%s.%s schema reference", schemaName, propertyName)
        .isEqualTo(expectedSchemaName);
  }

  private static void assertEnumSchema(
      JsonNode openApi,
      String schemaName,
      String... values
  ) {
    final var schema = openApi
        .path("components")
        .path("schemas")
        .path(schemaName);

    assertThat(schema.isMissingNode())
        .as("%s schema exists", schemaName)
        .isFalse();
    assertThat(schema.path("type").asText())
        .as("%s schema type", schemaName)
        .isEqualTo("string");
    assertThat(StreamSupport.stream(schema.path("enum").spliterator(), false)
        .map(JsonNode::asText)
        .toList())
        .as("%s enum values", schemaName)
        .containsExactly(values);
  }

  private static void assertEnumPropertyValues(
      JsonNode openApi,
      String schemaName,
      String propertyName,
      String... values
  ) {
    final var schema = resolveSchema(openApi, schemaProperty(openApi, schemaName, propertyName));

    assertThat(hasType(schema, "string")).isTrue();
    assertThat(StreamSupport.stream(schema.path("enum").spliterator(), false)
        .map(JsonNode::asText)
        .toList())
        .as("%s.%s enum values", schemaName, propertyName)
        .containsExactly(values);
  }

  private static String referencedSchemaName(JsonNode schema) {
    final var ref = schema.path("$ref").asText(null);
    assertThat(ref)
        .as("schema reference")
        .isNotBlank();
    return ref.substring(ref.lastIndexOf('/') + 1);
  }

  private static void assertRequiredProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var required = requiredProperties(openApi, schemaName);
    assertThat(required).contains(propertyNames);
  }

  private static void assertNotRequiredProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var required = requiredProperties(openApi, schemaName);
    assertThat(required).doesNotContain(propertyNames);
  }

  private static void assertMissingProperties(
      JsonNode openApi,
      String schemaName,
      String... propertyNames
  ) {
    final var properties = openApi
        .path("components")
        .path("schemas")
        .path(schemaName)
        .path("properties");
    assertThat(List.of(propertyNames))
        .as("%s properties", schemaName)
        .noneMatch(properties::has);
  }

  private static List<String> requiredProperties(
      JsonNode openApi,
      String schemaName
  ) {
    return StreamSupport.stream(openApi
            .path("components")
            .path("schemas")
            .path(schemaName)
            .path("required")
            .spliterator(), false)
        .map(JsonNode::asText)
        .toList();
  }

  private static void assertNullable(JsonNode property) {
    final var nullable = property.path("nullable").asBoolean(false)
        || hasType(property, "null")
        || StreamSupport.stream(property.path("anyOf").spliterator(), false)
        .anyMatch(schema -> hasType(schema, "null"));

    assertThat(nullable).isTrue();
  }

  private static void assertParameterRequired(
      JsonNode openApi,
      String path,
      String method,
      String parameterName,
      boolean required
  ) {
    assertThat(operationParameter(openApi, path, method, parameterName)
        .path("required")
        .asBoolean(false))
        .as("%s %s parameter %s required", method, path, parameterName)
        .isEqualTo(required);
  }

  private static void assertResponseCodes(
      JsonNode openApi,
      String path,
      String method,
      String... responseCodes
  ) {
    final var responses = operation(openApi, path, method).path("responses");

    assertThat(List.of(responseCodes))
        .allSatisfy(responseCode -> assertThat(responses.has(responseCode))
            .as("%s %s response %s", method, path, responseCode)
            .isTrue());
  }

  private static void assertOneOfSchema(
      JsonNode openApi,
      String schemaName
  ) {
    final var schema = openApi.path("components").path("schemas").path(schemaName);
    assertThat(schema.path("oneOf").isArray())
        .as("%s should remain a discriminated oneOf union", schemaName)
        .isTrue();
  }

  private static void assertNoAllOfSchema(
      JsonNode openApi,
      String schemaName
  ) {
    final var schema = openApi.path("components").path("schemas").path(schemaName);
    assertThat(schema.has("allOf"))
        .as("%s should be a flat object schema for TypeScript generators", schemaName)
        .isFalse();
    assertThat(schema.path("type").asText())
        .as("%s type", schemaName)
        .isEqualTo("object");
  }

  private static void assertNoSchemaComposesDiscriminatedUnionThroughAllOf(JsonNode openApi) {
    final var schemas = openApi.path("components").path("schemas");
    final var schemaProperties = schemas.properties();
    final var discriminatedUnionSchemaNames = schemaProperties.stream()
        .filter(entry -> entry.getValue().path("oneOf").isArray())
        .map(Map.Entry::getKey)
        .toList();

    final var recursiveCompositionRefs = schemaProperties.stream()
        .filter(entry -> entry.getValue().path("allOf").isArray())
        .flatMap(entry -> StreamSupport.stream(entry.getValue().path("allOf").spliterator(), false)
            .map(part -> part.path("$ref").asText())
            .filter(ref -> !ref.isBlank())
            .map(ref -> entry.getKey() + " -> " + ref.substring(ref.lastIndexOf('/') + 1)))
        .filter(ref -> discriminatedUnionSchemaNames.stream().anyMatch(ref::endsWith))
        .toList();

    assertThat(recursiveCompositionRefs)
        .as("schemas must not compose discriminated oneOf unions through allOf")
        .isEmpty();
  }

  private static Stream<String> accountDtoDescriptionViolations(
      JsonNode schemas,
      String schemaName
  ) {
    final var schema = schemas.path(schemaName);
    if (schema.isMissingNode()) {
      return Stream.of(schemaName + " schema is missing");
    }

    final var properties = schema.path("properties");
    if (!properties.isObject()) {
      return Stream.of(schemaName + " properties are missing");
    }

    return properties.properties().stream()
        .filter(property -> property.getValue().path("description").asText().isBlank())
        .map(property -> schemaName + "." + property.getKey() + " description is missing");
  }

  private static void assertStringFormat(
      JsonNode property,
      String format
  ) {
    assertThat(hasType(property, "string")).isTrue();
    assertThat(property.path("format").asText()).isEqualTo(format);
  }

  private static void assertExclusiveMinimumZero(JsonNode property) {
    final var exclusiveMinimum = property.path("exclusiveMinimum");

    assertThat(exclusiveMinimum.isMissingNode())
        .as("property has exclusive minimum: %s", property)
        .isFalse();
    if (exclusiveMinimum.isBoolean()) {
      assertThat(property.path("minimum").asText()).isEqualTo("0");
      assertThat(exclusiveMinimum.asBoolean()).isTrue();
      return;
    }

    assertThat(exclusiveMinimum.asText()).isEqualTo("0");
  }

  private static boolean hasType(
      JsonNode property,
      String type
  ) {
    if (type.equals(property.path("type").asText())) {
      return true;
    }

    return StreamSupport.stream(property.path("type").spliterator(), false)
        .map(JsonNode::asText)
        .anyMatch(type::equals);
  }

  private static void assertArray(JsonNode property) {
    assertThat(property.path("type").asText()).isEqualTo("array");
    assertThat(property.path("items").isMissingNode()).isFalse();
  }

  private static void assertEnumProperty(
      JsonNode openApi,
      String schemaName,
      String propertyName,
      String expectedValue
  ) {
    final var propertySchema = resolveSchema(openApi, schemaProperty(openApi, schemaName, propertyName));
    final var schema = hasType(propertySchema, "string")
        ? propertySchema
        : StreamSupport.stream(propertySchema.path("anyOf").spliterator(), false)
            .map(candidate -> resolveSchema(openApi, candidate))
            .filter(candidate -> hasType(candidate, "string"))
            .findFirst()
            .orElse(propertySchema);

    assertThat(hasType(schema, "string")).isTrue();
    assertThat(StreamSupport.stream(schema.path("enum").spliterator(), false)
        .map(JsonNode::asText)
        .toList())
        .contains(expectedValue);
  }

  private static void assertNoLegacyPhysicalQuantityResponseFields(@NonNull JsonNode openApi) {
    final var legacyFields = Set.of(
        "originalGravityUnit",
        "predictedFinalGravityUnit",
        "sugarUnit",
        "acidityUnit",
        "tanninUnit",
        "yeastAssimilableNitrogenUnit",
        "totalVolumeLiters",
        "totalVolumeUnit",
        "requestedVolumeLiters",
        "requestedVolumeUnit",
        "availableVolumeLiters",
        "availableVolumeUnit",
        "volumeLiters",
        "volumeUnit"
    );

    final var violations = openApi.path("components").path("schemas").properties().stream()
        .filter(entry -> entry.getKey().endsWith("Response"))
        .flatMap(entry -> entry.getValue().path("properties").properties().stream()
            .filter(property -> legacyFields.contains(property.getKey()))
            .map(property -> entry.getKey() + "." + property.getKey()))
        .sorted()
        .toList();

    assertThat(violations)
        .as("response schemas must not expose legacy physical quantity fields")
        .isEmpty();
  }

  private static void assertNoBareNumericResponsePropertiesOutsideAllowList(@NonNull JsonNode openApi) {
    final var allowedBareNumericProperties = Set.of(
        "DecimalQuantityResponse.value",
        "CompositionOptimizationObjectiveAssessmentResponse.score",
        "CompositionOptimizationPenaltyResponse.value",
        "ProfileTargetDistanceResponse.value",
        "ChemicalProfileCompositionSourceResponse.proportion",
        "FeedstockBatchCompositionOptimizationSourceResponse.proportion"
    );

    final var violations = openApi.path("components").path("schemas").properties().stream()
        .filter(entry -> entry.getKey().endsWith("Response"))
        .flatMap(entry -> entry.getValue().path("properties").properties().stream()
            .filter(property -> hasType(property.getValue(), "number"))
            .map(property -> entry.getKey() + "." + property.getKey()))
        .filter(propertyName -> !allowedBareNumericProperties.contains(propertyName))
        .sorted()
        .toList();

    assertThat(violations)
        .as("response schemas must wrap physical numeric values into DecimalQuantityResponse")
        .isEmpty();
  }

  private static JsonNode resolveSchema(
      JsonNode openApi,
      JsonNode schema
  ) {
    final var ref = schema.path("$ref").asText();
    if (ref.isBlank()) {
      return schema;
    }

    final var schemaName = ref.substring(ref.lastIndexOf('/') + 1);
    final var resolved = openApi.path("components").path("schemas").path(schemaName);

    assertThat(resolved.isMissingNode())
        .as("%s schema exists", schemaName)
        .isFalse();
    return resolved;
  }
}
