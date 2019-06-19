package com.sequenceiq.redbeams.converter.stack;

import com.sequenceiq.cloudbreak.cloud.model.StackTags;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.common.mappable.ProviderParameterCalculator;
import com.sequenceiq.cloudbreak.exception.BadRequestException;
import com.sequenceiq.redbeams.api.endpoint.v4.databaseserver.requests.AllocateDatabaseServerV4Request;
import com.sequenceiq.redbeams.api.endpoint.v4.stacks.DatabaseServerV4Request;
import com.sequenceiq.redbeams.api.endpoint.v4.stacks.NetworkV4Request;
import com.sequenceiq.redbeams.domain.stack.DBStack;
import com.sequenceiq.redbeams.domain.stack.DatabaseServer;
import com.sequenceiq.redbeams.domain.stack.Network;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

@Component
public class AllocateDatabaseServerV4RequestToDBStackConverter {

    @Inject
    private ProviderParameterCalculator providerParameterCalculator;

    // mimic CreateFreeIpaRequestToStackConverter
    public DBStack convert(AllocateDatabaseServerV4Request source, String accountId, String userId, String cloudPlatform) {
        DBStack dbStack = new DBStack();
        dbStack.setName(source.getName());
        dbStack.setEnvironmentId(source.getEnvironmentId());

        if (source.getNetwork() != null) {
            dbStack.setNetwork(buildNetwork(source.getNetwork()));
            // source.getNetwork().setCloudPlatform(CloudPlatform.valueOf(cloudPlatform));
        }
        if (source.getDatabaseServer() != null) {
            // source.getNetwork().setCloudPlatform(CloudPlatform.valueOf(cloudPlatform));
            dbStack.setDatabaseServer(buildDatabaseServer(source.getDatabaseServer()));
        }

        // dbStack.setTags(getTags(owner, cloudPlatform));

        Map<String, Object> asMap = providerParameterCalculator.get(source).asMap();
        if (asMap != null) {
            Map<String, String> parameter = new HashMap<>();
            asMap.forEach((key, value) -> parameter.put(key, value.toString()));
            dbStack.setParameters(parameter);
        }

        // dbStack.setOwner(Optional.of(userId).orElse(accountId));

        return dbStack;
    }

    private Network buildNetwork(NetworkV4Request source) {
        Network network = new Network();
        // network.setName(missingResourceNameGenerator.generateName(APIResourceType.NETWORK));

        Map<String, Object> parameters = providerParameterCalculator.get(source).asMap();
        if (parameters != null) {
            try {
                network.setAttributes(new Json(parameters));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid network parameters", e);
            }
        }
        return network;
    }

    private DatabaseServer buildDatabaseServer(DatabaseServerV4Request source) {
        DatabaseServer server = new DatabaseServer();

        return server;
    }

    // private Json getTags(String owner, String cloudPlatform) {
    //     try {
    //         return new Json(new StackTags(new HashMap<>(), new HashMap<>(), getDefaultTags(owner, cloudPlatform)));
    //     } catch (Exception ignored) {
    //         throw new BadRequestException("Failed to convert dynamic tags.");
    //     }
    // }

    // private Map<String, String> getDefaultTags(String owner, String cloudPlatform) {
    //     Map<String, String> result = new HashMap<>();
    //     try {
    //         result.putAll(costTaggingService.prepareDefaultTags(owner, result, cloudPlatform));
    //     } catch (Exception e) {
    //         LOGGER.debug("Exception during reading default tags.", e);
    //     }
    //     return result;
    // }

}

