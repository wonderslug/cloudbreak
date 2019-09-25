package com.sequenceiq.cloudbreak.cm.polling.service;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cm.ClouderaManagerOperationFailedException;
import com.sequenceiq.cloudbreak.cm.polling.ClouderaManagerCommandPollerObject;

@Service
public class ClouderaManagerKerberosConfigureListenerService extends AbstractClouderaManagerCommandCheckerService<ClouderaManagerCommandPollerObject> {

    @Override
    public void handleTimeout(ClouderaManagerCommandPollerObject toolsResourceApi) {
        throw new ClouderaManagerOperationFailedException("Operation timed out. Failed to configure kerberos Cloudera Manager services.");
    }

    @Override
    public String successMessage(ClouderaManagerCommandPollerObject toolsResourceApi) {
        return "Cloudera Manager succesfully configure Kerberos.";
    }

    @Override
    protected String getCommandName() {
        return "Configure for kerberos";
    }
}
