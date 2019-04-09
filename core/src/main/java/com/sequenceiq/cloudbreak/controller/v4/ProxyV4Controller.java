package com.sequenceiq.cloudbreak.controller.v4;

import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.stereotype.Controller;

import com.sequenceiq.cloudbreak.api.endpoint.v4.proxies.ProxyV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.proxies.requests.ProxyV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.proxies.responses.ProxyV4Response;
import com.sequenceiq.cloudbreak.api.endpoint.v4.proxies.responses.ProxyV4Responses;
import com.sequenceiq.cloudbreak.api.util.ConverterUtil;
import com.sequenceiq.cloudbreak.domain.ProxyConfig;
import com.sequenceiq.cloudbreak.message.NotificationEventType;
import com.sequenceiq.cloudbreak.service.proxy.ProxyConfigService;
import com.sequenceiq.cloudbreak.workspace.controller.WorkspaceEntityType;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;
import com.sequenceiq.notification.NotificationController;

@Controller
@Transactional(TxType.NEVER)
@WorkspaceEntityType(ProxyConfig.class)
public class ProxyV4Controller extends NotificationController implements ProxyV4Endpoint {

    @Inject
    private ProxyConfigService proxyConfigService;

    @Inject
    private ConverterUtil converterUtil;

    @Override
    public ProxyV4Responses list(Long workspaceId, String environment, Boolean attachGlobal) {
        Set<ProxyConfig> allInWorkspaceAndEnvironment = proxyConfigService
                .findAllByWorkspaceId(workspaceId);
        return new ProxyV4Responses(converterUtil.convertAllAsSet(allInWorkspaceAndEnvironment, ProxyV4Response.class));
    }

    @Override
    public ProxyV4Response get(Long workspaceId, String name) {
        ProxyConfig config = proxyConfigService.getByNameForWorkspaceId(name, workspaceId);
        return converterUtil.convert(config, ProxyV4Response.class);
    }

    @Override
    public ProxyV4Response post(Long workspaceId, ProxyV4Request request) {
        ProxyConfig config = converterUtil.convert(request, ProxyConfig.class);
        config = proxyConfigService.createForLoggedInUser(config, workspaceId);
        ProxyV4Response response = converterUtil.convert(config, ProxyV4Response.class);
        notify(response, NotificationEventType.CREATE_SUCCESS, WorkspaceResource.PROXY);
        return response;
    }

    @Override
    public ProxyV4Response delete(Long workspaceId, String name) {
        ProxyConfig deleted = proxyConfigService.deleteByNameFromWorkspace(name, workspaceId);
        ProxyV4Response response = converterUtil.convert(deleted, ProxyV4Response.class);
        notify(response, NotificationEventType.DELETE_SUCCESS, WorkspaceResource.PROXY);
        return response;
    }

    @Override
    public ProxyV4Responses deleteMultiple(Long workspaceId, Set<String> names) {
        Set<ProxyConfig> deleted = proxyConfigService.deleteMultipleByNameFromWorkspace(names, workspaceId);
        ProxyV4Responses response = new ProxyV4Responses(converterUtil.convertAllAsSet(deleted, ProxyV4Response.class));
        notify(response, NotificationEventType.DELETE_SUCCESS, WorkspaceResource.PROXY);
        return response;
    }

    @Override
    public ProxyV4Request getRequest(Long workspaceId, String name) {
        ProxyConfig proxyConfig = proxyConfigService.getByNameForWorkspaceId(name, workspaceId);
        return converterUtil.convert(proxyConfig, ProxyV4Request.class);
    }
}
