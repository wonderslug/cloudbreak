package com.sequenceiq.redbeams.converter.stack;

import com.sequenceiq.redbeams.domain.stack.DBStack;
import com.sequenceiq.redbeams.flow.redbeams.provision.event.allocate.AllocateDatabaseServerRequest;

import org.springframework.stereotype.Component;

@Component
public class AllocateDatabaseServerRequestToDBStackConverter {

    public DBStack convert(AllocateDatabaseServerRequest source, String accountId, String userId, String cloudPlatform) {
        DBStack dbStack = new DBStack();
        // mimic CreateFreeIpaRequestToStackConverter
        return dbStack;
    }

}

