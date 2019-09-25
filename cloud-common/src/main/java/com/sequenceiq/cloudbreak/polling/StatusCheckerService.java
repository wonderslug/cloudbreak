package com.sequenceiq.cloudbreak.polling;

public interface StatusCheckerService<T> {

    boolean checkStatus(T t);

    void handleTimeout(T t);

    String successMessage(T t);

    boolean exitPolling(T t);

    void handleException(Exception e);
}
