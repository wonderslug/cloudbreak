package com.sequenceiq.sdx.api.endpoint;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.validation.annotation.Validated;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.UpgradeOptionV4Response;
import com.sequenceiq.cloudbreak.jerseyclient.retry.RetryingRestClient;
import com.sequenceiq.cloudbreak.validation.ValidCrn;
import com.sequenceiq.cloudbreak.validation.ValidStackNameFormat;
import com.sequenceiq.cloudbreak.validation.ValidStackNameLength;
import com.sequenceiq.sdx.api.model.RedeploySdxClusterRequest;
import com.sequenceiq.sdx.api.model.SdxClusterDetailResponse;
import com.sequenceiq.sdx.api.model.SdxClusterRequest;
import com.sequenceiq.sdx.api.model.SdxClusterResponse;
import com.sequenceiq.sdx.api.model.SdxRepairRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Validated
@Path("/sdx")
@RetryingRestClient
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/sdx", protocols = "http,https", consumes = MediaType.APPLICATION_JSON)
public interface SdxEndpoint {

    @POST
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create SDX cluster", produces = "application/json", nickname = "createSdx")
    SdxClusterResponse create(@ValidStackNameFormat @ValidStackNameLength @PathParam("name") String name,
            @Valid SdxClusterRequest createSdxClusterRequest);

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "list SDX clusters", produces = MediaType.APPLICATION_JSON, nickname = "listSdx")
    List<SdxClusterResponse> list(@QueryParam("envName") String envName);

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get SDX cluster", produces = MediaType.APPLICATION_JSON, nickname = "getSdx")
    SdxClusterResponse get(@PathParam("name") String name);

    @GET
    @Path("/crn/{clusterCrn}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get SDX cluster by crn", produces = MediaType.APPLICATION_JSON, nickname = "getSdxByCrn")
    SdxClusterResponse getByCrn(@PathParam("clusterCrn") @ValidCrn String clusterCrn);

    @GET
    @Path("/envcrn/{envCrn}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get SDX cluster by environment crn", produces = MediaType.APPLICATION_JSON, nickname = "getSdxByEnvCrn")
    List<SdxClusterResponse> getByEnvCrn(@PathParam("envCrn") @ValidCrn String envCrn);

    @GET
    @Path("{name}/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get SDX cluster detail", produces = MediaType.APPLICATION_JSON, nickname = "getSdxDetail")
    SdxClusterDetailResponse getDetail(@PathParam("name") String name, @QueryParam("entries") Set<String> entries);

    @GET
    @Path("/crn/{clusterCrn}/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get SDX cluster detail by crn", produces = MediaType.APPLICATION_JSON, nickname = "getSdxDetailByCrn")
    SdxClusterDetailResponse getDetailByCrn(@PathParam("clusterCrn") String clusterCrn, @QueryParam("entries") Set<String> entries);

    @DELETE
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete SDX cluster", produces = "application/json", nickname = "deleteSdx")
    void delete(@PathParam("name") String name, @QueryParam("forced") @DefaultValue("false") Boolean forced);

    @DELETE
    @Path("/crn/{clusterCrn}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete SDX cluster by crn", produces = "application/json", nickname = "deleteSdxByCrn")
    void deleteByCrn(@PathParam("clusterCrn") String clusterCrn, @QueryParam("forced") @DefaultValue("false") Boolean forced);

    @POST
    @Path("{name}/redeploy")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "redeploy SDX cluster", produces = MediaType.APPLICATION_JSON, nickname = "redeploySdx")
    void redeploy(@PathParam("name") String name, @Valid RedeploySdxClusterRequest redeploySdxClusterRequest);

    @POST
    @Path("/crn/{clusterCrn}/redeploy")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "redeploy SDX cluster by crn", produces = MediaType.APPLICATION_JSON, nickname = "redeploySdxByCrn")
    void redeployByCrn(@PathParam("clusterCrn") String clusterCrn, @Valid RedeploySdxClusterRequest redeploySdxClusterRequest);

    @POST
    @Path("{name}/manual_repair")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "repairs an sdxNode in the specified hostgroup", nickname = "repairSdxNode")
    void repairCluster(@PathParam("name") String name, SdxRepairRequest clusterRepairRequest);

    @POST
    @Path("/crn/{crn}/manual_repair")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "repairs an sdxNode in the specified hostgroup by crn", nickname = "repairSdxNodeByCrn")
    void repairClusterByCrn(@PathParam("crn") String crn, SdxRepairRequest clusterRepairRequest);

    @GET
    @Path("{name}/check_for_upgrade")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "checks for upgrade options by name", nickname = "checkForUpgrade")
    UpgradeOptionV4Response checkForUpgradeByName(@PathParam("name") String name);

    @GET
    @Path("/crn/{crn}/check_for_upgrade")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "checks for upgrade options by crn", nickname = "checkForUpgradeByCrn")
    UpgradeOptionV4Response checkForUpgradeByCrn(@PathParam("crn") String crn);

    @POST
    @Path("{name}/upgrade")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "upgrades the datalake cluster to the latest images", nickname = "upgradeDatalakeCluster")
    void upgradeClusterByName(@PathParam("name") String name);

    @POST
    @Path("/crn/{crn}/upgrade")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "upgrades the datalake cluster to the latest images", nickname = "upgradeDatalakeClusterByCrn")
    void upgradeClusterByCrn(@PathParam("crn") String crn);

    @POST
    @Path("{name}/sync")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "sync SDX cluster by name", produces = MediaType.APPLICATION_JSON, nickname = "syncSdx")
    void sync(@PathParam("name") String name);

    @POST
    @Path("/crn/{crn}/sync")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "sync SDX cluster by crn", produces = MediaType.APPLICATION_JSON, nickname = "syncSdxByCrn")
    void syncByCrn(@PathParam("crn") String crn);

    @POST
    @Path("{name}/retry")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "retry sdx", produces = MediaType.APPLICATION_JSON, nickname = "retrySdx")
    void retry(@PathParam("name") String name);

    @POST
    @Path("/crn/{crn}/retry")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "retry sdx by crn", produces = MediaType.APPLICATION_JSON, nickname = "retrySdxByCrn")
    void retryByCrn(@PathParam("crn") String crn);

    @POST
    @Path("{name}/start")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "start sdx", produces = MediaType.APPLICATION_JSON, nickname = "startSdxByName")
    void startByName(@PathParam("name") String name);

    @POST
    @Path("/crn/{crn}/start")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "start sdx by crn", produces = MediaType.APPLICATION_JSON, nickname = "startSdxByCrn")
    void startByCrn(@PathParam("crn") String crn);

    @POST
    @Path("{name}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "stop sdx", produces = MediaType.APPLICATION_JSON, nickname = "stopSdxByName")
    void stopByName(@PathParam("name") String name);

    @POST
    @Path("/crn/{crn}/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "stop sdx by crn", produces = MediaType.APPLICATION_JSON, nickname = "stopSdxByCrn")
    void stopByCrn(@PathParam("crn") String crn);
}
