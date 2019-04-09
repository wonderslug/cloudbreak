package com.sequenceiq.cloudbreak.reactor;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.event.credential.InteractiveCredentialCreationRequest;
import com.sequenceiq.cloudbreak.cloud.model.ExtendedCloudCredential;
import com.sequenceiq.cloudbreak.controller.mapper.DuplicatedKeyValueExceptionMapper;
import com.sequenceiq.cloudbreak.converter.spi.ExtendedCloudCredentialToCredentialConverter;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.exception.BadRequestException;
import com.sequenceiq.cloudbreak.message.NotificationEventType;
import com.sequenceiq.cloudbreak.service.DuplicateKeyValueException;
import com.sequenceiq.cloudbreak.service.credential.CredentialService;
import com.sequenceiq.cloudbreak.service.user.UserService;
import com.sequenceiq.cloudbreak.structuredevent.event.responses.CloudbreakV4Event;
import com.sequenceiq.cloudbreak.workspace.model.User;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;
import com.sequenceiq.notification.Notification;
import com.sequenceiq.notification.NotificationAssemblingService;
import com.sequenceiq.notification.NotificationSender;

import reactor.bus.Event;

@Component
public class InteractiveCredentialCreationHandler implements EventHandler<InteractiveCredentialCreationRequest> {

    @Inject
    private CredentialService credentialService;

    @Inject
    private NotificationSender notificationSender;

    @Inject
    private ExtendedCloudCredentialToCredentialConverter extendedCloudCredentialToCredentialConverter;

    @Inject
    private UserService userService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(InteractiveCredentialCreationRequest.class);
    }

    @Override
    public void accept(Event<InteractiveCredentialCreationRequest> interactiveCredentialCreationRequestEvent) {
        InteractiveCredentialCreationRequest interactiveCredentialCreationRequest = interactiveCredentialCreationRequestEvent.getData();

        ExtendedCloudCredential extendedCloudCredential = interactiveCredentialCreationRequest.getExtendedCloudCredential();
        Credential credential = extendedCloudCredentialToCredentialConverter.convert(extendedCloudCredential);
        User user = userService.getOrCreate(extendedCloudCredential.getCloudbreakUser());
        try {
            credentialService.initCodeGrantFlow(extendedCloudCredential.getWorkspaceId(), credential, user);
            sendNotification(extendedCloudCredential, extendedCloudCredential.getName(), NotificationEventType.CREDENTIAL_APP_CREATED);
        } catch (DuplicateKeyValueException e) {
            sendNotification(extendedCloudCredential, DuplicatedKeyValueExceptionMapper.errorMessage(e), NotificationEventType.CREATE_FAILED);
        } catch (BadRequestException e) {
            sendNotification(extendedCloudCredential, e.getMessage(), NotificationEventType.CREATE_FAILED);
        }
    }

    private void sendNotification(ExtendedCloudCredential extendedCloudCredential, String message, NotificationEventType eventType) {
        CloudbreakV4Event notification = NotificationAssemblingService
                .cloudbreakEvent(extendedCloudCredential, eventType, WorkspaceResource.CREDENTIAL.getShortName());
        notification.setUser(extendedCloudCredential.getUserId());
        notificationSender.send(new Notification<>(notification));
    }
}
