package com.sequenceiq.cloudbreak.service.audit;

import static com.sequenceiq.cloudbreak.exception.NotFoundException.notFound;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.audits.responses.AuditEventV4Response;
import com.sequenceiq.cloudbreak.api.util.ConverterUtil;
import com.sequenceiq.cloudbreak.comparator.audit.AuditEventComparator;
import com.sequenceiq.cloudbreak.domain.StructuredEventEntity;
import com.sequenceiq.cloudbreak.service.AbstractWorkspaceAwareResourceService;
import com.sequenceiq.cloudbreak.service.RestRequestThreadLocalService;
import com.sequenceiq.cloudbreak.service.user.UserService;
import com.sequenceiq.cloudbreak.service.workspace.WorkspaceService;
import com.sequenceiq.cloudbreak.structuredevent.db.StructuredEventRepository;
import com.sequenceiq.cloudbreak.workspace.model.User;
import com.sequenceiq.cloudbreak.workspace.model.Workspace;
import com.sequenceiq.cloudbreak.workspace.repository.workspace.WorkspaceResourceRepository;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;

@Service
public class AuditEventService extends AbstractWorkspaceAwareResourceService<StructuredEventEntity> {

    @Inject
    private ConverterUtil converterUtil;

    @Inject
    private StructuredEventRepository structuredEventRepository;

    @Inject
    private WorkspaceService workspaceService;

    @Inject
    private UserService userService;

    @Inject
    private RestRequestThreadLocalService restRequestThreadLocalService;

    public AuditEventV4Response getAuditEvent(Long auditId) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getCloudbreakUser());
        return getAuditEventByWorkspaceId(workspaceService.getDefaultWorkspaceForUser(user).getId(), auditId);
    }

    public AuditEventV4Response getAuditEventByWorkspaceId(Long workspaceId, Long auditId) {
        StructuredEventEntity event = Optional.ofNullable(structuredEventRepository.findByWorkspaceIdAndId(workspaceId, auditId))
                .orElseThrow(notFound("StructuredEvent", auditId));
        return converterUtil.convert(event, AuditEventV4Response.class);
    }

    public List<AuditEventV4Response> getAuditEventsByWorkspaceId(Long workspaceId, String resourceType, String identifier) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getCloudbreakUser());
        Workspace workspace = getWorkspaceService().get(workspaceId, user);
        List<StructuredEventEntity> events = getEventsForUserWithTypeAndIdentifierByWorkspace(workspace, resourceType, identifier);
        List<AuditEventV4Response> auditEventV4Responses = events != null ? converterUtil.convertAll(events, AuditEventV4Response.class) : new LinkedList<>();
        auditEventV4Responses.sort(new AuditEventComparator().reversed());
        return auditEventV4Responses;
    }

    private List<StructuredEventEntity> getEventsForUserWithTypeAndIdentifierByWorkspace(Workspace workspace, String resourceType, String identifier) {
        Optional<Long> resourceId = getIdentifierAsLongIfAvailable(identifier);
        if (resourceId.isPresent()) {
            return structuredEventRepository.findByWorkspaceAndResourceTypeAndResourceId(workspace, resourceType, resourceId.get());
        }
        return structuredEventRepository.findByWorkspaceAndResourceTypeAndResourceCrn(workspace, resourceType, identifier);
    }

    @Override
    public WorkspaceResourceRepository<StructuredEventEntity, Long> repository() {
        return structuredEventRepository;
    }

    @Override
    public WorkspaceResource resource() {
        return WorkspaceResource.STRUCTURED_EVENT;
    }

    @Override
    protected void prepareDeletion(StructuredEventEntity resource) {

    }

    @Override
    protected void prepareCreation(StructuredEventEntity resource) {

    }

    private Optional<Long> getIdentifierAsLongIfAvailable(String identifier) {
        try {
            return Optional.of(Long.parseLong(identifier));
        } catch (NumberFormatException ignore) {
            return Optional.empty();
        }
    }

}
