package com.sequenceiq.cloudbreak.polling;

import com.sequenceiq.cloudbreak.common.exception.CloudbreakServiceException;

public abstract class SimpleStatusCheckerService<T> implements StatusCheckerService<T> {

    @Override
    public void handleException(Exception e) {
        throw new CloudbreakServiceException(e);
    }

}
