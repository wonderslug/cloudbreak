package com.sequenceiq.cloudbreak.controller.v4;

import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.stereotype.Controller;

import com.sequenceiq.cloudbreak.api.endpoint.v4.blueprint.BlueprintV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.blueprint.requests.BlueprintV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.blueprint.responses.BlueprintV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.blueprint.responses.BlueprintV4Responses;
import com.sequenceiq.cloudbreak.api.endpoint.v4.blueprint.responses.BlueprintV4ViewResponse;
import com.sequenceiq.cloudbreak.api.endpoint.v4.blueprint.responses.BlueprintV4ViewResponses;
import com.sequenceiq.cloudbreak.api.endpoint.v4.util.responses.ParametersQueryV4Response;
import com.sequenceiq.cloudbreak.api.util.ConverterUtil;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.view.BlueprintView;
import com.sequenceiq.cloudbreak.message.NotificationEventType;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import com.sequenceiq.cloudbreak.workspace.controller.WorkspaceEntityType;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;
import com.sequenceiq.notification.NotificationController;

@Controller
@Transactional(TxType.NEVER)
@WorkspaceEntityType(Blueprint.class)
public class BlueprintV4Controller extends NotificationController implements BlueprintV4Endpoint {

    @Inject
    private BlueprintService blueprintService;

    @Inject
    private ConverterUtil converterUtil;

    @Override
    public BlueprintV4ViewResponses list(Long workspaceId) {
        Set<BlueprintView> allAvailableViewInWorkspace = blueprintService.getAllAvailableViewInWorkspace(workspaceId);
        return new BlueprintV4ViewResponses(converterUtil.convertAllAsSet(allAvailableViewInWorkspace, BlueprintV4ViewResponse.class));
    }

    @Override
    public BlueprintV4Response get(Long workspaceId, String name) {
        Blueprint blueprint = blueprintService.getByNameForWorkspaceId(name, workspaceId);
        return converterUtil.convert(blueprint, BlueprintV4Response.class);
    }

    @Override
    public BlueprintV4Response post(Long workspaceId, BlueprintV4Request request) {
        Blueprint blueprint = blueprintService.createForLoggedInUser(
                converterUtil.convert(request, Blueprint.class), workspaceId);
        BlueprintV4Response response = converterUtil.convert(blueprint, BlueprintV4Response.class);
        notify(response, NotificationEventType.CREATE_SUCCESS, WorkspaceResource.BLUEPRINT);
        return response;
    }

    @Override
    public BlueprintV4Response delete(Long workspaceId, String name) {
        Blueprint blueprint = blueprintService.deleteByNameFromWorkspace(name, workspaceId);
        BlueprintV4Response response = converterUtil.convert(blueprint, BlueprintV4Response.class);
        notify(response, NotificationEventType.DELETE_SUCCESS, WorkspaceResource.BLUEPRINT);
        return response;
    }

    @Override
    public BlueprintV4Responses deleteMultiple(Long workspaceId, Set<String> names) {
        Set<Blueprint> deleted = blueprintService.deleteMultipleByNameFromWorkspace(names, workspaceId);
        Set<BlueprintV4Response> response = converterUtil.convertAllAsSet(deleted, BlueprintV4Response.class);
        notify(response, NotificationEventType.DELETE_SUCCESS, WorkspaceResource.BLUEPRINT);
        return new BlueprintV4Responses(response);
    }

    @Override
    public BlueprintV4Request getRequest(Long workspaceId, String name) {
        Blueprint blueprint = blueprintService.getByNameForWorkspaceId(name, workspaceId);
        return converterUtil.convert(blueprint, BlueprintV4Request.class);
    }

    @Override
    public ParametersQueryV4Response getParameters(Long workspaceId, String name) {
        ParametersQueryV4Response parametersQueryV4Response = new ParametersQueryV4Response();
        parametersQueryV4Response.setCustom(blueprintService.queryCustomParametersMap(name, workspaceId));
        return parametersQueryV4Response;
    }

}
