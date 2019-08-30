package com.sequenceiq.cloudbreak.service.blueprint;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.blueprint.AmbariBlueprintProcessorFactory;
import com.sequenceiq.cloudbreak.cmtemplate.CmTemplateProcessorFactory;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.service.hostgroup.HostGroupService;
import com.sequenceiq.cloudbreak.service.stack.InstanceGroupService;
import com.sequenceiq.cloudbreak.template.processor.BlueprintTextProcessor;

@Service
public class ComponentLocatorService {

    @Inject
    private AmbariBlueprintProcessorFactory ambariBlueprintProcessorFactory;

    @Inject
    private CmTemplateProcessorFactory cmTemplateProcessorFactory;

    @Inject
    private HostGroupService hostGroupService;

    @Inject
    private BlueprintService blueprintService;

    @Inject
    private InstanceGroupService instanceGroupService;

    public Map<String, List<String>> getComponentLocation(Cluster cluster, Collection<String> componentNames) {
        return getComponentAttribute(cluster, componentNames, InstanceMetaData::getDiscoveryFQDN);
    }

    public Map<String, List<String>> getComponentLocationByPrivateIp(Cluster cluster, Collection<String> componentNames) {
        return getComponentAttribute(cluster, componentNames, InstanceMetaData::getPrivateIp);
    }

    public Map<String, List<String>> getComponentLocation(Long stackId, BlueprintTextProcessor blueprintTextProcessor,
            Collection<String> componentNames) {
        return getComponentAttribute(stackId, blueprintTextProcessor, componentNames, InstanceMetaData::getDiscoveryFQDN);
    }

    private Map<String, List<String>> getComponentAttribute(Cluster cluster, Collection<String> componentNames, Function<InstanceMetaData, String> fqdn) {
        Map<String, List<String>> result = new HashMap<>();
        String blueprintText = cluster.getBlueprint().getBlueprintText();
        BlueprintTextProcessor processor = isAmbariBlueprint(cluster) ? ambariBlueprintProcessorFactory.get(blueprintText)
                : cmTemplateProcessorFactory.get(blueprintText);
        Set<InstanceGroup> instanceGroups = instanceGroupService.findByStackId(cluster.getStack().getId());
        for (InstanceGroup instanceGroup : instanceGroups) {
            Set<String> hgComponents = new HashSet<>(processor.getComponentsInHostGroup(instanceGroup.getGroupName()));
            hgComponents.retainAll(componentNames);
            fillList(fqdn, result, instanceGroup, hgComponents);
        }
        return result;
    }

    private boolean isAmbariBlueprint(Cluster cluster) {
        return blueprintService.isAmbariBlueprint(cluster.getBlueprint());
    }

    private Map<String, List<String>> getComponentAttribute(Long stackId, BlueprintTextProcessor blueprintTextProcessor,
            Collection<String> componentNames, Function<InstanceMetaData, String> fqdn) {
        Map<String, List<String>> result = new HashMap<>();
        Set<InstanceGroup> instanceGroups = instanceGroupService.findByStackId(stackId);
        for (InstanceGroup instanceGroup : instanceGroups) {
            Set<String> hgComponents = new HashSet<>(blueprintTextProcessor.getComponentsInHostGroup(instanceGroup.getGroupName()));
            hgComponents.retainAll(componentNames);
            fillList(fqdn, result, instanceGroup, hgComponents);
        }
        return result;
    }

    private void fillList(Function<InstanceMetaData, String> fqdn, Map<String, List<String>> result, InstanceGroup instanceGroup, Set<String> hgComponents) {
        List<String> attributeList = instanceGroup.getNotDeletedInstanceMetaDataSet().stream().map(fqdn).collect(Collectors.toList());
        for (String service : hgComponents) {
            List<String> storedAttributes = result.get(service);
            if (storedAttributes == null) {
                result.put(service, attributeList);
            } else {
                storedAttributes.addAll(attributeList);
            }
        }
    }
}
