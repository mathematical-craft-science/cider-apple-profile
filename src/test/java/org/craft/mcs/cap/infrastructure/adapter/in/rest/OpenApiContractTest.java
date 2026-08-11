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
import org.craft.mcs.cap.core.account.actor.application.port.in.GetActorDisplayUseCase;
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
import org.craft.mcs.cap.core.measurement.application.port.in.CorrectMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.QueryFeedstockPlanningBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.*;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.application.port.in.UpdateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.production.candidate.application.port.in.GetProductionBatchCandidateUseCase;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.*;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.*;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.port.in.CompositionGraphInterpretationUseCase;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.vessel.application.port.in.CreateVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.in.UpdateVesselUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.AppleVarietyJsonSeedImporter;
import org.craft.mcs.cap.infrastructure.adapter.in.measurement.MeasurementValueInputAcl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.CompositionRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.AppleVarietyProfileTargetCandidateRequestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper.AppleVarietyProfileGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper.FeedstockBatchSugarAcidityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.MeasurementSetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper.ProductionBatchCandidateRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.JolicoeurSugarAcidityGraphMetadataResponseProvider;
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
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;
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
    CompositionRequestMapper.class,
    AppleVarietyProfileTargetCandidateRequestMapper.class,
    ProductionBatchCandidateRestMapper.class,
    JolicoeurSugarAcidityGraphMetadataResponseProvider.class,
    MeasurementValueInputAcl.class,
    MeasurementSetRestMapper.class
})
@MockitoBean(types = {
    GetActorDisplayUseCase.class,
    ContributorProfileUseCase.class,
    AppleVarietyProfileGraphRestMapper.class,
    FeedstockBatchSugarAcidityRestMapper.class,
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
    QueryFeedstockBatchProfileTargetCandidatesUseCase.class,
    ScoreFeedstockBatchProfileTargetUseCase.class,
    AssessFeedstockBatchCurrentFermentationStartReadinessUseCase.class,
    AssessFeedstockBatchCurrentSugarAcidityZoneUseCase.class,
    QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase.class,
    CreateFeedstockBatchUseCase.class,
    DeleteFeedstockBatchUseCase.class,
    ListFeedstockBatchesUseCase.class,
    UpdateFeedstockBatchUseCase.class,
    GetFeedstockBatchDetailUseCase.class,
    CreateMeasurementSetUseCase.class,
    CorrectMeasurementSetUseCase.class,
    QueryMeasurementSetsUseCase.class,
    RetractMeasurementSetUseCase.class,
    QueryFeedstockPlanningBatchesUseCase.class,
    OptimizeFeedstockBatchCompositionUseCase.class,
    CreateProductionBatchUseCase.class,
    CreateProductionBatchCandidateUseCase.class,
    GetProductionBatchCandidateUseCase.class,
    GetProductionBatchUseCase.class,
    ListProductionBatchesUseCase.class,
    UpdateProductionBatchUseCase.class,
    PotentialAbvPredictor.class,
    CalculateCompositionUseCase.class,
    OptimizeCompositionUseCase.class,
    CompositionGraphInterpretationUseCase.class,
    JolicoeurSugarAcidityZoneGeometryProfile.class,
    SugarAcidityTargetResolver.class,
    CreateVesselUseCase.class,
    GetVesselUseCase.class,
    ListAvailableVesselsUseCase.class,
    ListVesselsUseCase.class,
    UpdateVesselUseCase.class,
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
  void openApiDescribesProviderNeutralActorDirectory() throws Exception {
    final var openApi = openApi();

    assertJsonResponse(
        openApi,
        "/account/actors/{actorType}/{actorId}",
        "get"
    );
    assertResponseCodes(
        openApi,
        "/account/actors/{actorType}/{actorId}",
        "get",
        "200",
        "401",
        "403",
        "422",
        "404"
    );
    assertRequiredProperties(
        openApi,
        "ActorDisplayResponse",
        "type",
        "id",
        "displayName",
        "status"
    );
    assertNotRequiredProperties(openApi, "ActorDisplayResponse", "avatarUrl");
    assertNullable(schemaProperty(openApi, "ActorDisplayResponse", "avatarUrl"));
    assertEnumPropertyValues(
        openApi,
        "ActorDisplayResponse",
        "type",
        "ACCOUNT_USER",
        "MACHINE_IDENTITY"
    );
    assertEnumPropertyValues(
        openApi,
        "ActorDisplayResponse",
        "status",
        "ACTIVE",
        "INACTIVE",
        "DELETED"
    );
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
  void openApiDoesNotExposeRemovedLegacyMeasurementEndpoints() throws Exception {
    final var openApi = openApi();

    assertThat(List.of(
        openApi.path("paths").has("/feedstock-batches/{batchId}/measurements"),
        openApi.path("paths").has("/feedstock-batches/{batchId}/measurements/{measurementId}"),
        openApi.path("paths").has("/feedstock-measurements/{measurementId}/sugar-acidity-zone"),
        openApi.path("paths").has("/feedstock-measurements/sugar-acidity-zone-points"),
        openApi.path("paths").has("/feedstock-measurements/{measurementId}/abv-prediction")
    )).containsOnly(false);
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
    final var retractionReason = operationParameter(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "delete",
        "reason"
    );

    assertEnumPropertyValues(
        openApi,
        "MeasurementProvenanceRequest",
        "sourceType",
        "USER",
        "INSTRUMENT",
        "LAB",
        "IMPORT",
        "CURATOR",
        "INSTITUTE"
    );

    assertThat(List.of(
        expectedVersion.path("required").asText(),
        expectedVersionSchema.path("type").asText(),
        expectedVersionSchema.path("format").asText(),
        expectedVersionSchema.path("minimum").asText()
    )).containsExactly("true", "integer", "int64", "0");
    assertThat(List.of(
        retractionReason.path("required").asText(),
        retractionReason.path("schema").path("type").asText(),
        retractionReason.path("schema").path("minLength").asText(),
        retractionReason.path("schema").path("maxLength").asText()
    )).containsExactly("true", "string", "1", "1000");

    assertResponseCodes(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "put",
        "200",
        "404",
        "409",
        "422"
    );
    assertResponseCodes(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "patch",
        "200",
        "404",
        "409",
        "422"
    );
    assertResponseCodes(
        openApi,
        "/measurement-sets/{measurementSetId}",
        "delete",
        "204",
        "404",
        "409",
        "422"
    );
    assertResponseCodes(
        openApi,
        "/measurement-sets",
        "get",
        "200",
        "403",
        "404"
    );
  }

  @Test
  void openApiDescribesWorkspaceProfileSummaryAsCompositeEvidence() throws Exception {
    final var description = openApi()
        .path("components")
        .path("schemas")
        .path("FeedstockCurrentProfileSummaryResponse")
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
    assertJsonResponse(openApi, "/profile-analysis/graphs/jolicoeur-sugar-acidity/metadata", "get");
    assertThat(openApi.path("paths").has("/chemical-profiles/sugar-acidity-zone/plot-metadata")).isFalse();
    assertJsonResponse(openApi, "/measurement-units/metadata", "get");
    assertJsonResponse(openApi, "/measurement-sets", "post", "201");
    assertJsonResponse(openApi, "/measurement-sets", "get");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}", "get");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}/revisions", "get");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}", "put");
    assertJsonResponse(openApi, "/measurement-sets/{measurementSetId}", "patch");
    assertJsonResponse(openApi, "/material-subjects/{subjectType}/{subjectId}/current-state", "get");
    assertJsonResponse(openApi, "/composition-calculations", "post");
    assertJsonResponse(openApi, "/composition-optimizations", "post");
    final var paths = openApi.path("paths");
    assertThat(List.of(
        paths.path("/chemical-profile-compositions/calculations").isMissingNode(),
        paths.path("/chemical-profile-compositions/optimizations").isMissingNode()
    )).containsOnly(true);
    assertJsonResponse(openApi, "/feedstock-batches", "get");
    assertJsonResponse(openApi, "/feedstock-batches", "post", "201");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "put");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}", "patch");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/sugar-acidity-zone", "get");
    assertJsonResponse(openApi, "/feedstock-batches/sugar-acidity-zone-points", "post");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/abv-prediction", "get");
    assertJsonResponse(openApi, "/feedstock-batches/{batchId}/fermentation-start-readiness", "get");
    assertJsonResponse(openApi, "/feedstock-batches/profile-target-scores", "post");
    assertJsonResponse(openApi, "/feedstock-batches/composition-optimizations", "post");
    assertJsonResponse(openApi, "/feedstock-workspace/batches", "post");
    assertJsonResponse(openApi, "/production-batches", "get");
    assertJsonResponse(openApi, "/production-batches", "post", "201");
    assertJsonResponse(openApi, "/production-batches/{batchId}", "get");
    assertJsonResponse(openApi, "/production-batches/{batchId}", "put");
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
    assertJsonRequest(openApi, "/account/workspaces/{workspaceId}/settings", "patch");
    assertJsonRequest(openApi, "/apple-varieties/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/analysis/apple-varieties/profile-target-candidates", "post");
    assertJsonRequest(openApi, "/analysis/feedstock-batches/profile-target-candidates", "post");
    assertJsonRequest(openApi, "/composition-calculations", "post");
    assertJsonRequest(openApi, "/composition-optimizations", "post");
    assertJsonRequest(openApi, "/feedstock-batches/sugar-acidity-zone-points", "post");
    assertJsonRequest(openApi, "/feedstock-batches/profile-target-scores", "post");
    assertJsonRequest(openApi, "/feedstock-batches/composition-optimizations", "post");
    assertJsonRequest(openApi, "/feedstock-workspace/batches", "post");
    assertJsonRequest(openApi, "/production-batches", "post");
    assertJsonRequest(openApi, "/production-batches/{batchId}", "put");
    assertJsonRequest(openApi, "/account/profile", "patch");
    assertJsonRequest(openApi, "/account/preferences", "patch");

    assertStringFormat(schemaProperty(openApi, "AppleVarietyClassificationResponse", "varietyId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockSeasonResponse", "startsOn"), "date");
    assertArray(schemaProperty(openApi, "AppleVarietySugarAcidityZonePointBatchResponse", "points"));
    assertArray(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointBatchResponse", "points"));
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "batchId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "currentMaterialStateId"), "uuid");
    assertArray(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "profileEvidence"));
    assertArray(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "missingCharacteristics"));
    assertRequiredProperties(openApi, "FeedstockBatchGraphPointResponse",
        "batchId", "batchName", "workspaceId", "currentMaterialStateId", "profileEvidence",
        "plottable", "missingCharacteristics");
    assertMissingProperties(openApi, "FeedstockBatchGraphPointResponse", "missingAxes");
    assertNotRequiredProperties(openApi, "FeedstockBatchGraphPointResponse",
        "point", "zone", "northAmericanTanninLevel");
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "point"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "zone"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchGraphPointResponse", "northAmericanTanninLevel"));
    assertMissingProperties(
        openApi,
        "FeedstockBatchGraphPointResponse",
        "currentMeasurementId",
        "measuredAt",
        "assessment",
        "capabilityAssessment"
    );
    assertMissingProperties(openApi, "FeedstockCurrentProfileGraphResponse", "point");
    assertRequiredProperties(
        openApi,
        "FeedstockCurrentProfileGraphResponse",
        "plottable",
        "missingCharacteristics"
    );
    assertNotRequiredProperties(openApi, "FeedstockCurrentProfileGraphResponse", "assessment");
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileGraphResponse", "assessment"));
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
    assertMissingSchema(openApi, "ProfileAnalysisCapabilityAssessmentResponse");
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
        "vesselId",
        "type",
        "season"
    );
    assertMissingProperties(openApi, "CreateFeedstockBatchRequest", "vessel");
    assertUuidV7(schemaProperty(openApi, "CreateFeedstockBatchRequest", "vesselId"));
    assertNotRequiredProperties(openApi, "CreateFeedstockBatchRequest", "source", "note");
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "CreateFeedstockBatchRequest", "note"));
    assertExclusiveMinimumZero(schemaProperty(openApi, "CreateFeedstockBatchRequest", "availableVolumeLiters"));

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
        "JolicoeurSugarAcidityGraphMetadataResponse",
        "specificGravityAxis",
        "titratableAcidityAxis",
        "zones"
    );
    assertMissingProperties(openApi, "JolicoeurSugarAcidityGraphMetadataResponse", "zoneSystem");
    assertNotRequiredProperties(
        openApi,
        "AppleVarietySugarAcidityZonePointQueryRequest",
        "nameContains",
        "zone",
        "plottable",
        "after",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "AppleVarietySugarAcidityZonePointQueryRequest", "plottable"));
    assertMissingProperties(
        openApi,
        "AppleVarietySugarAcidityZonePointQueryRequest",
        "capability",
        "capable"
    );
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
        "zone",
        "plottable",
        "after",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "batchId"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "season"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "zone"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest", "plottable"));
    assertMissingProperties(
        openApi,
        "FeedstockBatchCurrentSugarAcidityZonePointQueryRequest",
        "capability",
        "capable"
    );

    assertParameterRequired(openApi, "/feedstock-batches", "get", "workspaceId", true);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonStartsOn", false);
    assertParameterRequired(openApi, "/feedstock-batches", "get", "seasonEndsOn", false);

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
        "resultRetractions",
        "createdAt",
        "updatedAt"
    );
    assertStringFormat(schemaProperty(openApi, "MeasurementSetResponse", "materialStateId"), "uuid");
    assertArray(schemaProperty(openApi, "MeasurementSetResponse", "resultRetractions"));
    assertRequiredProperties(
        openApi,
        "MeasurementSetRevisionHistoryResponse",
        "measurementSetId",
        "lifecycle",
        "version",
        "revisions"
    );
    assertArray(schemaProperty(openApi, "MeasurementSetRevisionHistoryResponse", "revisions"));
    assertRequiredProperties(
        openApi,
        "MeasurementSetRevisionResponse",
        "revisionId",
        "revisionNumber",
        "materialStateId",
        "name",
        "createdAt",
        "actor",
        "records",
        "resultRetractions"
    );
    assertRequiredProperties(
        openApi,
        "MeasurementResultRetractionResponse",
        "recordId",
        "characteristic",
        "status",
        "resultSnapshot",
        "observationSnapshots",
        "sourceRevisionId",
        "sourceRevisionNumber",
        "actor",
        "retractedAt",
        "reason"
    );
    assertStringFormat(
        schemaProperty(openApi, "MeasurementResultRetractionResponse", "recordId"),
        "uuid"
    );
    assertStringFormat(
        schemaProperty(openApi, "MeasurementResultRetractionResponse", "sourceRevisionId"),
        "uuid"
    );
    assertArray(schemaProperty(openApi, "MeasurementResultRetractionResponse", "observationSnapshots"));
    assertEnumPropertyValues(
        openApi,
        "MeasurementResultRetractionResponse",
        "status",
        "RETRACTED"
    );
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
        "correctionReason",
        "retraction"
    );
    assertNullable(schemaProperty(openApi, "MeasurementSetResponse", "retraction"));
    assertRequiredProperties(
        openApi,
        "MeasurementSetRetractionResponse",
        "actor",
        "retractedAt",
        "reason"
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
        "provenance"
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
        "freshnessStatus"
    );
    assertStringFormat(
        schemaProperty(openApi, "CurrentMaterialCharacteristicResponse", "evidenceMaterialStateId"),
        "uuid"
    );
    assertMissingSchema(openApi, "ChemicalMeasurementResponse");
    assertMissingSchema(openApi, "ExactChemicalMeasurementResponse");
    assertMissingSchema(openApi, "RangeChemicalMeasurementResponse");
    assertMissingSchema(openApi, "ChemicalProfileResponse");
    assertMissingSchema(openApi, "FeedstockMeasurementResponse");
    assertMissingSchema(openApi, "FeedstockMeasurementChemicalProfileResponse");
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
        "CalculatedCompositionResponse",
        "sources",
        "characteristics",
        "potentialAbv",
        "warnings"
    );
    assertNotRequiredProperties(
        openApi,
        "CalculatedCompositionResponse",
        "totalVolume",
        "northAmericanTanninLevel",
        "normalizerCode",
        "targetDistance"
    );
    assertNullable(schemaProperty(openApi, "CalculatedCompositionResponse", "totalVolume"));
    assertNullable(schemaProperty(openApi, "CalculatedCompositionResponse", "northAmericanTanninLevel"));
    assertEnumProperty(
        openApi,
        "CalculatedCompositionResponse",
        "northAmericanTanninLevel",
        "LOW"
    );
    assertEnumProperty(openApi, "CalculatedCompositionResponse", "northAmericanTanninLevel", "MEDIUM");
    assertEnumProperty(openApi, "CalculatedCompositionResponse", "northAmericanTanninLevel", "HIGH");
    assertRequiredProperties(openApi, "CompositionSourceResponse", "reference");
    assertNotRequiredProperties(openApi, "CompositionSourceResponse", "requestedVolume", "proportion");
    assertNullable(schemaProperty(openApi, "CompositionSourceResponse", "requestedVolume"));
    assertRequiredProperties(
        openApi,
        "CompositionOptimizationCandidateResponse",
        "sources",
        "characteristics",
        "potentialAbv",
        "warnings",
        "targetDistance",
        "objectiveAssessment"
    );
    assertNotRequiredProperties(
        openApi,
        "CompositionOptimizationCandidateResponse",
        "totalVolume",
        "northAmericanTanninLevel"
    );
    assertNullable(schemaProperty(openApi, "CompositionOptimizationCandidateResponse", "totalVolume"));
    assertNullable(
        schemaProperty(openApi, "CompositionOptimizationCandidateResponse", "northAmericanTanninLevel")
    );
    assertEnumProperty(
        openApi,
        "CompositionOptimizationCandidateResponse",
        "northAmericanTanninLevel",
        "LOW"
    );
    assertEnumProperty(
        openApi,
        "CompositionOptimizationCandidateResponse",
        "northAmericanTanninLevel",
        "MEDIUM"
    );
    assertEnumProperty(
        openApi,
        "CompositionOptimizationCandidateResponse",
        "northAmericanTanninLevel",
        "HIGH"
    );
    assertRequiredProperties(
        openApi,
        "CompositionOptimizationPenaltyResponse",
        "code",
        "severity",
        "value"
    );
    assertNotRequiredProperties(
        openApi,
        "CompositionOptimizationPenaltyResponse",
        "characteristic",
        "actualValue",
        "threshold"
    );
    assertEnumProperty(
        openApi,
        "CompositionOptimizationPenaltyResponse",
        "characteristic",
        "YAN"
    );
    assertNotRequiredProperties(
        openApi,
        "AppleVarietySugarAcidityZoneResponse",
        "tannin",
        "northAmericanProfile",
        "potentialAbv"
    );
    assertNullable(schemaProperty(openApi, "AppleVarietySugarAcidityZoneResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "AppleVarietySugarAcidityZoneResponse", "northAmericanProfile"));
    assertNullable(schemaProperty(openApi, "AppleVarietySugarAcidityZoneResponse", "potentialAbv"));
    assertRequiredProperties(
        openApi,
        "AppleVarietySugarAcidityZoneResponse",
        "varietyId",
        "varietyName",
        "plottable",
        "missingCharacteristics"
    );
    assertMissingProperties(
        openApi,
        "AppleVarietySugarAcidityZoneResponse",
        "capabilityAssessment"
    );
    assertNotRequiredProperties(
        openApi,
        "AppleVarietyNorthAmericanProfileResponse",
        "sugar",
        "acidity",
        "tannin"
    );
    assertNullable(schemaProperty(openApi, "AppleVarietyNorthAmericanProfileResponse", "sugar"));
    assertNullable(schemaProperty(openApi, "AppleVarietyNorthAmericanProfileResponse", "acidity"));
    assertNullable(schemaProperty(openApi, "AppleVarietyNorthAmericanProfileResponse", "tannin"));
    assertMissingProperties(openApi, "CalculateCompositionRequest", "scoringPurpose");
    assertMissingProperties(openApi, "OptimizeCompositionRequest", "scoringPurpose");
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
        "tanninTarget",
        "weights",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "nameContains"));
    assertNullable(schemaProperty(openApi, "AppleVarietyProfileTargetCandidateSearchRequest", "zone"));
    assertMissingProperties(
        openApi,
        "AppleVarietyProfileTargetCandidateSearchRequest",
        "capability",
        "capable"
    );
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
        "tanninTarget",
        "weights",
        "limit"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "batchIds"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "season"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchProfileTargetCandidateSearchRequest", "zone"));
    assertMissingProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateSearchRequest",
        "capability",
        "capable"
    );
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
        "profileEvidence",
        "characteristics",
        "distance"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockBatchProfileTargetScoreResponse", "currentMaterialStateId"),
        "uuid"
    );
    assertMissingProperties(
        openApi,
        "FeedstockBatchProfileTargetScoreResponse",
        "currentMeasurementId",
        "currentMeasurementName"
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
        "profileEvidence",
        "characteristics",
        "distance",
        "contributionRole"
    );
    assertMissingProperties(
        openApi,
        "FeedstockBatchProfileTargetCandidateResponse",
        "currentMeasurementId",
        "currentMeasurementName"
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
  void openApiDescribesWorkspaceVesselCatalogAndBatchReferences() throws Exception {
    final var openApi = openApi();

    assertThat(operation(openApi, "/vessels", "post").path("responses").has("201")).isTrue();
    assertThat(operation(openApi, "/vessels", "get").path("responses").has("200")).isTrue();
    assertThat(operation(openApi, "/vessels/available", "get").path("responses").has("200")).isTrue();
    assertThat(operation(openApi, "/vessels/available", "get").path("responses").has("400")).isTrue();
    assertThat(operation(openApi, "/vessels/{vesselId}", "get").path("responses").has("404")).isTrue();
    assertThat(operation(openApi, "/vessels/{vesselId}", "put").path("responses").has("409")).isTrue();
    assertParameterRequired(openApi, "/vessels", "get", "workspaceId", true);
    assertParameterRequired(openApi, "/vessels/available", "get", "workspaceId", true);
    assertParameterRequired(openApi, "/vessels/available", "get", "nameContains", false);
    assertParameterRequired(openApi, "/vessels/available", "get", "requiredVolumeLiters", false);
    assertExclusiveMinimumZero(
        operationParameter(openApi, "/vessels/available", "get", "requiredVolumeLiters").path("schema")
    );
    assertParameterRequired(openApi, "/vessels/{vesselId}", "get", "vesselId", true);
    assertUuidV7(operationParameter(openApi, "/vessels/{vesselId}", "get", "vesselId").path("schema"));
    assertUuidV7(operationParameter(openApi, "/vessels/{vesselId}", "put", "vesselId").path("schema"));

    assertRequiredProperties(
        openApi,
        "CreateVesselRequest",
        "workspaceId",
        "name",
        "nominalCapacityLiters",
        "material"
    );
    assertStringFormat(schemaProperty(openApi, "CreateVesselRequest", "workspaceId"), "uuid");
    assertExclusiveMinimumZero(schemaProperty(openApi, "CreateVesselRequest", "nominalCapacityLiters"));
    assertEnumProperty(openApi, "CreateVesselRequest", "material", "STAINLESS_STEEL");
    assertEnumProperty(openApi, "CreateVesselRequest", "material", "FIBERGLASS");
    assertRequiredProperties(
        openApi,
        "UpdateVesselRequest",
        "workspaceId",
        "expectedVersion",
        "name",
        "nominalCapacityLiters",
        "material",
        "operationalStatus"
    );
    assertThat(List.of(
        schemaProperty(openApi, "UpdateVesselRequest", "expectedVersion").path("type").asText(),
        schemaProperty(openApi, "UpdateVesselRequest", "expectedVersion").path("format").asText(),
        schemaProperty(openApi, "UpdateVesselRequest", "expectedVersion").path("minimum").asText()
    )).containsExactly("integer", "int64", "0");
    assertRequiredProperties(
        openApi,
        "VesselResponse",
        "vesselId",
        "workspaceId",
        "name",
        "nominalCapacity",
        "material",
        "operationalStatus",
        "usedVolume",
        "remainingCapacity",
        "usage",
        "version"
    );
    assertUuidV7(schemaProperty(openApi, "VesselResponse", "vesselId"));
    assertEnumProperty(openApi, "VesselResponse", "operationalStatus", "OPERATIONAL");
    assertNotRequiredProperties(openApi, "VesselResponse", "currentContent");
    assertNullableReference(
        schemaProperty(openApi, "VesselResponse", "currentContent"),
        "VesselCurrentContentResponse"
    );
    assertEnumProperty(openApi, "VesselResponse", "usage", "PARTIALLY_FILLED");
    assertRequiredProperties(
        openApi,
        "VesselCurrentContentResponse",
        "subjectType",
        "subjectId",
        "materialStateId",
        "displayName",
        "volume"
    );
    assertEnumProperty(openApi, "VesselCurrentContentResponse", "subjectType", "FEEDSTOCK_BATCH");
    assertUuidV7(schemaProperty(openApi, "VesselCurrentContentResponse", "subjectId"));
    assertUuidV7(schemaProperty(openApi, "VesselCurrentContentResponse", "materialStateId"));
    assertRequiredProperties(openApi, "VesselReferenceResponse", "vesselId", "name");
    assertUuidV7(schemaProperty(openApi, "VesselReferenceResponse", "vesselId"));
    assertRequiredProperties(openApi, "ProductionBatchResponse", "vessel");
    assertRequiredProperties(openApi, "ProductionBatchDetailsResponse", "vessel");
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
        "vesselId",
        "candidateId"
    );
    assertStringFormat(schemaProperty(openApi, "CreateProductionBatchRequest", "workspaceId"), "uuid");
    assertUuidV7(schemaProperty(openApi, "CreateProductionBatchRequest", "vesselId"));
    assertStringFormat(schemaProperty(openApi, "CreateProductionBatchRequest", "candidateId"), "uuid");
    assertNotRequiredProperties(openApi, "CreateProductionBatchRequest", "note");
    assertNullable(schemaProperty(openApi, "CreateProductionBatchRequest", "note"));
    assertRequiredProperties(
        openApi,
        "UpdateProductionBatchRequest",
        "workspaceId",
        "name",
        "vesselId"
    );
    assertStringFormat(schemaProperty(openApi, "UpdateProductionBatchRequest", "workspaceId"), "uuid");
    assertUuidV7(schemaProperty(openApi, "UpdateProductionBatchRequest", "vesselId"));
    assertNotRequiredProperties(openApi, "UpdateProductionBatchRequest", "note");
    assertNullable(schemaProperty(openApi, "UpdateProductionBatchRequest", "note"));

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
        "currentProfile",
        "potentialAbv"
    );
    assertStringFormat(schemaProperty(openApi, "ProductionBatchDetailsResponse", "workspaceId"), "uuid");
    assertStringFormat(schemaProperty(openApi, "ProductionBatchDetailsResponse", "currentMaterialStateId"), "uuid");
    assertNotRequiredProperties(
        openApi,
        "ProductionBatchDetailsResponse",
        "note"
    );
    assertNullable(schemaProperty(openApi, "ProductionBatchDetailsResponse", "note"));
    assertMissingProperties(
        openApi,
        "ProductionBatchDetailsResponse",
        "currentChemicalProfile",
        "measurementEvidence",
        "yeastAssimilableNitrogen"
    );

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
    assertRequiredProperties(openApi, "BatchTraceMaterialSnapshotResponse", "characteristics");
    assertArray(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "characteristics"));
    assertNotRequiredProperties(openApi, "BatchTraceMaterialSnapshotResponse", "volume", "confidence");
    assertNullable(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "volume"));
    assertNullable(schemaProperty(openApi, "BatchTraceMaterialSnapshotResponse", "confidence"));
    assertRequiredProperties(
        openApi,
        "BatchTraceCharacteristicResponse",
        "characteristic",
        "value"
    );
    assertEnumProperty(
        openApi,
        "BatchTraceCharacteristicResponse",
        "characteristic",
        "SPECIFIC_GRAVITY"
    );

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
        "vessel",
        "type",
        "season",
        "availableVolume"
    );
    assertNotRequiredProperties(openApi, "UpdateFeedstockBatchRequest", "vesselId", "source", "note");
    assertNotRequiredProperties(openApi, "FeedstockBatchResponse", "source", "note");
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "source"));
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "note"));
    assertNullable(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "vesselId"));
    assertUuidV7(schemaProperty(openApi, "UpdateFeedstockBatchRequest", "vesselId"));
    assertThat(referencedSchemaName(schemaProperty(openApi, "FeedstockBatchResponse", "vessel")))
        .isEqualTo("VesselReferenceResponse");
    assertMissingProperties(openApi, "UpdateFeedstockBatchRequest", "availableVolumeLiters", "vessel");
    assertRequiredProperties(openApi, "PatchFeedstockBatchRequest", "workspaceId");
    assertNotRequiredProperties(
        openApi,
        "PatchFeedstockBatchRequest",
        "vesselId",
        "name",
        "type",
        "season",
        "source",
        "note"
    );
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "name"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "vesselId"));
    assertUuidV7(schemaProperty(openApi, "PatchFeedstockBatchRequest", "vesselId"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "season"));
    assertNullable(schemaProperty(openApi, "PatchFeedstockBatchRequest", "note"));
    assertMissingProperties(openApi, "PatchFeedstockBatchRequest", "availableVolumeLiters", "vessel");

    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentPotentialAbvResponse",
        "batchId",
        "workspaceId",
        "currentMaterialStateId",
        "specificGravityEvidence",
        "originalGravityValueKind",
        "prediction"
    );
    assertStringFormat(
        schemaProperty(openApi, "FeedstockBatchCurrentPotentialAbvResponse", "currentMaterialStateId"),
        "uuid"
    );
    assertMissingProperties(
        openApi,
        "FeedstockBatchCurrentPotentialAbvResponse",
        "currentMeasurementId",
        "currentMeasurementName"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentFermentationStartReadinessResponse",
        "batchId",
        "workspaceId",
        "currentMaterialStateId",
        "profileEvidence",
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
    assertMissingProperties(
        openApi,
        "FeedstockBatchCurrentFermentationStartReadinessResponse",
        "currentMeasurementId",
        "currentMeasurementName"
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
        "profileEvidence",
        "planningReadiness",
        "dataQuality"
    );
    assertStringFormat(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "currentMaterialStateId"), "uuid");
    assertArray(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "profileEvidence"));
    assertNotRequiredProperties(
        openApi,
        "FeedstockWorkspaceBatchResponse",
        "currentMeasurement",
        "profileSummary",
        "measurementFreshness",
        "sugarAcidity",
        "potentialAbv",
        "fermentationStartReadiness",
        "targetCandidate"
    );
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "currentMeasurement"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "profileSummary"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "measurementFreshness"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "sugarAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "potentialAbv"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "fermentationStartReadiness"));
    assertNullable(schemaProperty(openApi, "FeedstockWorkspaceBatchResponse", "targetCandidate"));

    assertRequiredProperties(
        openApi,
        "FeedstockWorkspaceCurrentMeasurementResponse",
        "measurementId",
        "name",
        "measuredAt"
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
    assertMissingProperties(openApi, "FeedstockWorkspaceCurrentMeasurementResponse", "chemicalProfile");

    assertNotRequiredProperties(
        openApi,
        "FeedstockCurrentProfileSummaryResponse",
        "specificGravity",
        "titratableAcidity",
        "ph",
        "tannin",
        "yan"
    );
    assertNullable(
        schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "specificGravity")
    );
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "titratableAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "ph"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "yan"));

    assertRequiredProperties(openApi, "FeedstockCurrentProfileValueResponse", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "FeedstockCurrentProfileValueResponse",
        "min",
        "max"
    );
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileValueResponse", "max"));

    assertRequiredProperties(openApi, "FeedstockCurrentProfileFreshnessResponse", "status", "stale");
    assertNotRequiredProperties(openApi, "FeedstockCurrentProfileFreshnessResponse", "reason");
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileFreshnessResponse", "reason"));

    assertRequiredProperties(
        openApi,
        "FeedstockCurrentProfileGraphResponse",
        "plottable",
        "missingCharacteristics"
    );
    assertMissingProperties(openApi, "FeedstockCurrentProfileGraphResponse", "point");
    assertNotRequiredProperties(openApi, "FeedstockCurrentProfileGraphResponse", "assessment");
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileGraphResponse", "assessment"));

    assertRequiredProperties(
        openApi,
        "FeedstockCurrentProfilePotentialAbvResponse",
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
    assertRequiredProperties(openApi, "FeedstockPlanningReadinessResponse", "status", "checks");
    assertArray(schemaProperty(openApi, "FeedstockPlanningReadinessResponse", "checks"));
    assertRequiredProperties(
        openApi,
        "FeedstockPlanningReadinessCheckResponse",
        "factor",
        "severity",
        "code",
        "message"
    );
    assertEnumProperty(
        openApi,
        "FeedstockPlanningReadinessCheckResponse",
        "code",
        "SPECIFIC_GRAVITY_PRESENT"
    );
    assertRequiredProperties(openApi, "FeedstockDataQualityResponse", "status", "indicators");
    assertArray(schemaProperty(openApi, "FeedstockDataQualityResponse", "indicators"));
    assertRequiredProperties(
        openApi,
        "FeedstockDataQualityIndicatorResponse",
        "code",
        "severity",
        "message"
    );
    assertRequiredProperties(
        openApi,
        "FeedstockBatchDetailResponse",
        "batch",
        "currentMaterialStateId",
        "currentProfile"
    );
    assertStringFormat(schemaProperty(openApi, "FeedstockBatchDetailResponse", "currentMaterialStateId"), "uuid");
    assertMissingProperties(openApi, "FeedstockBatchDetailResponse", "measurements");
    assertRequiredProperties(
        openApi,
        "FeedstockBatchCurrentProfileResponse",
        "profileEvidence",
        "planningReadiness",
        "dataQuality"
    );
    assertArray(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "profileEvidence"));
    assertNotRequiredProperties(
        openApi,
        "FeedstockBatchCurrentProfileResponse",
        "profileSummary",
        "sugarAcidityAssessment",
        "potentialAbv",
        "measurementFreshness",
        "fermentationStartReadiness"
    );
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "profileSummary"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "sugarAcidityAssessment"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "potentialAbv"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "measurementFreshness"));
    assertNullable(schemaProperty(openApi, "FeedstockBatchCurrentProfileResponse", "fermentationStartReadiness"));
    assertMissingProperties(
        openApi,
        "FeedstockBatchCurrentProfileResponse",
        "latestMeasurement",
        "lastOperation"
    );

    assertNotRequiredProperties(
        openApi,
        "FeedstockCurrentProfileSummaryResponse",
        "specificGravity",
        "titratableAcidity",
        "ph",
        "tannin",
        "yan"
    );
    assertNullable(
        schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "specificGravity")
    );
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "titratableAcidity"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "ph"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "tannin"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileSummaryResponse", "yan"));

    assertRequiredProperties(openApi, "FeedstockCurrentProfileValueResponse", "value", "valueKind");
    assertNotRequiredProperties(
        openApi,
        "FeedstockCurrentProfileValueResponse",
        "min",
        "max"
    );
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileValueResponse", "min"));
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileValueResponse", "max"));

    assertRequiredProperties(openApi, "FeedstockCurrentProfileFreshnessResponse", "status", "stale");
    assertNotRequiredProperties(openApi, "FeedstockCurrentProfileFreshnessResponse", "reason");
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileFreshnessResponse", "reason"));
    assertRequiredProperties(
        openApi,
        "FeedstockCurrentProfileGraphResponse",
        "plottable",
        "missingCharacteristics"
    );
    assertMissingProperties(openApi, "FeedstockCurrentProfileGraphResponse", "point");
    assertNotRequiredProperties(openApi, "FeedstockCurrentProfileGraphResponse", "assessment");
    assertNullable(schemaProperty(openApi, "FeedstockCurrentProfileGraphResponse", "assessment"));
    assertRequiredProperties(
        openApi,
        "FeedstockCurrentProfilePotentialAbvResponse",
        "originalGravityValueKind",
        "prediction"
    );
    assertRequiredProperties(openApi, "FeedstockPlanningReadinessResponse", "status", "checks");
    assertArray(schemaProperty(openApi, "FeedstockPlanningReadinessResponse", "checks"));
    assertRequiredProperties(
        openApi,
        "FeedstockPlanningReadinessCheckResponse",
        "factor",
        "severity",
        "code",
        "message"
    );
    assertEnumProperty(
        openApi,
        "FeedstockPlanningReadinessCheckResponse",
        "code",
        "TITRATABLE_ACIDITY_MISSING"
    );
    assertRequiredProperties(openApi, "FeedstockDataQualityResponse", "status", "indicators");
    assertArray(schemaProperty(openApi, "FeedstockDataQualityResponse", "indicators"));
    assertRequiredProperties(
        openApi,
        "FeedstockDataQualityIndicatorResponse",
        "code",
        "severity",
        "message"
    );
    assertMissingSchema(openApi, "FeedstockBatchDetailLastOperationResponse");
    assertEnumSchema(openApi, "FeedstockCurrentProfileSignalSeverity", "OK", "INFO", "WARNING", "BLOCKING");
    assertMissingSchema(openApi, "FeedstockPlanningSignalSeverity");
    assertMissingSchema(openApi, "FeedstockPlanningReadinessSeverity");
    assertMissingSchema(openApi, "FeedstockPlanningDataQualitySeverity");
    assertPropertyReferencesSchema(
        openApi,
        "FeedstockPlanningReadinessCheckResponse",
        "severity",
        "FeedstockCurrentProfileSignalSeverity"
    );
    assertPropertyReferencesSchema(
        openApi,
        "FeedstockDataQualityIndicatorResponse",
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

  private static void assertNullableReference(
      JsonNode property,
      String schemaName
  ) {
    final var alternatives = StreamSupport.stream(property.path("anyOf").spliterator(), false)
        .map(schema -> schema.has("$ref")
            ? schema.path("$ref").asText()
            : "type:" + schema.path("type").asText())
        .toList();

    assertThat(property.has("type"))
        .as("nullable reference must not declare a conflicting root type")
        .isFalse();
    assertThat(alternatives)
        .containsExactlyInAnyOrder(
            "#/components/schemas/" + schemaName,
            "type:null"
        );
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

  private static void assertUuidV7(JsonNode property) {
    assertStringFormat(property, "uuid");
    assertThat(property.path("pattern").asText()).isEqualTo(PATTERN);
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
        "CompositionSourceResponse.proportion",
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
