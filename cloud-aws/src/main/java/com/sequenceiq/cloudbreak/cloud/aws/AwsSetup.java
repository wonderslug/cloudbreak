package com.sequenceiq.cloudbreak.cloud.aws;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceTypesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysRequest;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.InstanceTypeInfo;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.InternetGatewayAttachment;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.servicequotas.model.GetServiceQuotaRequest;
import com.sequenceiq.cloudbreak.cloud.Setup;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonAutoScalingRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonCloudFormationRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.client.AmazonServiceQuotaRetryClient;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsCredentialView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsInstanceView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsNetworkView;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZone;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudRegions;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.Group;
import com.sequenceiq.cloudbreak.cloud.model.Image;
import com.sequenceiq.cloudbreak.cloud.model.InstanceAuthentication;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.Location;
import com.sequenceiq.cloudbreak.cloud.model.SpiFileSystem;
import com.sequenceiq.cloudbreak.cloud.notification.PersistenceNotifier;
import com.sequenceiq.common.api.type.ImageStatus;
import com.sequenceiq.common.api.type.ImageStatusResult;

@Component
public class AwsSetup implements Setup {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsSetup.class);

    private static final String IGW_DOES_NOT_EXIST_MSG = "The given internet gateway '%s' does not exist or belongs to a different region.";

    private static final String SUBNET_DOES_NOT_EXIST_MSG = "The given subnet '%s' does not exist or belongs to a different region.";

    private static final String SUBNETVPC_DOES_NOT_EXIST_MSG = "The given subnet '%s' does not belong to the given VPC '%s'.";

    private static final String IGWVPC_DOES_NOT_EXIST_MSG = "The given internet gateway '%s' does not belong to the given VPC '%s'.";

    private static final String IMAGE_OPT_IN_REQUIRED_MSG = "Unable to create cluster because AWS Marketplace subscription to the Hortonworks Data Cloud"
            + " HDP Services is required. In order to create a cluster, you need to accept terms and subscribe to the AWS Marketplace product.";

    private static final String LINK_TO_MARKETPLACE_MSG = "To do so please visit ";

    private static final String MARKETPLACE_HTTP_LINK = "http://aws.amazon.com/marketplace";

    private static final int FINISHED_PROGRESS_VALUE = 100;

    private static final int UNAUTHORIZED = 403;

    @Value("${cb.aws.spotinstances.enabled:}")
    private boolean awsSpotinstanceEnabled;

    @Inject
    private CloudFormationStackUtil cfStackUtil;

    @Inject
    private AwsClient awsClient;

    @Inject
    private AwsPlatformResources awsPlatformResources;

    @Inject
    private AwsInstanceConnector instanceConnector;

    @Inject
    private AwsInstanceTypeQuotaCodesConfig instanceTypeQuotaCodesConfig;

    @Override
    public ImageStatusResult checkImageStatus(AuthenticatedContext authenticatedContext, CloudStack stack, Image image) {
        return new ImageStatusResult(ImageStatus.CREATE_FINISHED, FINISHED_PROGRESS_VALUE);
    }

    @Override
    public void prepareImage(AuthenticatedContext authenticatedContext, CloudStack stack, Image image) {
        LOGGER.debug("prepare image has been executed");
    }

    @Override
    public void prerequisites(AuthenticatedContext ac, CloudStack stack, PersistenceNotifier persistenceNotifier) {
        AwsNetworkView awsNetworkView = new AwsNetworkView(stack.getNetwork());
        AwsCredentialView credentialView = new AwsCredentialView(ac.getCloudCredential());
        String region = ac.getCloudContext().getLocation().getRegion().value();
        verifySpotInstances(stack);
        if (awsNetworkView.isExistingVPC()) {
            try {
                AmazonEC2Client amazonEC2Client = awsClient.createAccess(credentialView, region);
                validateExistingIGW(awsNetworkView, amazonEC2Client);
                validateExistingSubnet(awsNetworkView, amazonEC2Client);
            } catch (AmazonServiceException e) {
                throw new CloudConnectorException(e.getErrorMessage());
            } catch (AmazonClientException e) {
                throw new CloudConnectorException(e.getMessage());
            }

        }
        validateRegionAndZone(ac.getCloudCredential(), ac.getCloudContext().getLocation());
        validateExistingKeyPair(stack.getInstanceAuthentication(), credentialView, region);
        LOGGER.debug("setup has been executed");
    }

    private void validateRegionAndZone(CloudCredential cloudCredential, Location location) {
        CloudRegions regions = awsPlatformResources.regions(cloudCredential, location.getRegion(), Collections.emptyMap());
        List<AvailabilityZone> availabilityZones = regions.getCloudRegions().get(location.getRegion());
        if (!availabilityZones.contains(location.getAvailabilityZone())) {
            throw new CloudConnectorException(String.format("Region [%s] doesn't contain availability zone [%s]",
                    location.getRegion().getRegionName(), location.getAvailabilityZone().value()));
        }
    }

    private void verifySpotInstances(CloudStack stack) {
        if (!awsSpotinstanceEnabled) {
            for (Group group : stack.getGroups()) {
                if (group.getInstances() != null
                        && !group.getInstances().isEmpty()
                        && new AwsInstanceView(group.getReferenceInstanceConfiguration().getTemplate()).getSpotPrice() != null) {
                    throw new CloudConnectorException(String.format("Spot instances are not supported on this AMI: %s", stack.getImage()));
                }
            }
        }
    }

    private void validateExistingSubnet(AwsNetworkView awsNetworkView, AmazonEC2 amazonEC2Client) {
        if (awsNetworkView.isExistingSubnet()) {
            DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest();
            describeSubnetsRequest.withSubnetIds(awsNetworkView.getSubnetList());
            DescribeSubnetsResult describeSubnetsResult = amazonEC2Client.describeSubnets(describeSubnetsRequest);
            if (describeSubnetsResult.getSubnets().size() < awsNetworkView.getSubnetList().size()) {
                throw new CloudConnectorException(String.format(SUBNET_DOES_NOT_EXIST_MSG, awsNetworkView.getExistingSubnet()));
            } else {
                for (Subnet subnet : describeSubnetsResult.getSubnets()) {
                    String vpcId = subnet.getVpcId();
                    if (vpcId != null && !vpcId.equals(awsNetworkView.getExistingVpc())) {
                        throw new CloudConnectorException(String.format(SUBNETVPC_DOES_NOT_EXIST_MSG, awsNetworkView.getExistingSubnet(),
                                awsNetworkView.getExistingVpc()));
                    }
                }
            }
        }
    }

    private void validateExistingIGW(AwsNetworkView awsNetworkView, AmazonEC2 amazonEC2Client) {
        if (awsNetworkView.isExistingIGW()) {
            DescribeInternetGatewaysRequest describeInternetGatewaysRequest = new DescribeInternetGatewaysRequest();
            describeInternetGatewaysRequest.withInternetGatewayIds(awsNetworkView.getExistingIgw());
            DescribeInternetGatewaysResult describeInternetGatewaysResult = amazonEC2Client.describeInternetGateways(describeInternetGatewaysRequest);
            if (describeInternetGatewaysResult.getInternetGateways().size() < 1) {
                throw new CloudConnectorException(String.format(IGW_DOES_NOT_EXIST_MSG, awsNetworkView.getExistingIgw()));
            } else {
                InternetGateway internetGateway = describeInternetGatewaysResult.getInternetGateways().get(0);
                InternetGatewayAttachment attachment = internetGateway.getAttachments().get(0);
                if (attachment != null && !attachment.getVpcId().equals(awsNetworkView.getExistingVpc())) {
                    throw new CloudConnectorException(String.format(IGWVPC_DOES_NOT_EXIST_MSG, awsNetworkView.getExistingIgw(),
                            awsNetworkView.getExistingVpc()));
                }
            }
        }
    }

    @Override
    public void validateFileSystem(CloudCredential credential, SpiFileSystem spiFileSystem) {

    }

    @Override
    public void validateParameters(AuthenticatedContext authenticatedContext, Map<String, String> parameters) {

    }

    private void validateExistingKeyPair(InstanceAuthentication instanceAuthentication, AwsCredentialView credentialView, String region) {
        String keyPairName = awsClient.getExistingKeyPairName(instanceAuthentication);
        if (StringUtils.isNotEmpty(keyPairName)) {
            boolean keyPairIsPresentOnEC2 = false;
            try {
                AmazonEC2Client client = awsClient.createAccess(credentialView, region);
                DescribeKeyPairsResult describeKeyPairsResult = client.describeKeyPairs(new DescribeKeyPairsRequest().withKeyNames(keyPairName));
                keyPairIsPresentOnEC2 = describeKeyPairsResult.getKeyPairs().stream().findFirst().isPresent();
            } catch (RuntimeException e) {
                String errorMessage = String.format("Failed to get the key pair [name: '%s'] from EC2 [roleArn:'%s'], detailed message: %s.",
                        keyPairName, credentialView.getRoleArn(), e.getMessage());
                LOGGER.info(errorMessage, e);
            }
            if (!keyPairIsPresentOnEC2) {
                throw new CloudConnectorException(String.format("The key pair '%s' could not be found in the '%s' region of EC2.", keyPairName, region));
            }
        }
    }

    @Override
    public void scalingPrerequisites(AuthenticatedContext ac, CloudStack stack, boolean upscale) {
        if (!upscale) {
            return;
        }

        AmazonCloudFormationRetryClient cloudFormationClient = awsClient.createCloudFormationRetryClient(new AwsCredentialView(ac.getCloudCredential()),
                ac.getCloudContext().getLocation().getRegion().value());
        AmazonAutoScalingRetryClient amazonASClient = awsClient.createAutoScalingRetryClient(new AwsCredentialView(ac.getCloudCredential()),
                ac.getCloudContext().getLocation().getRegion().value());
        List<Group> groups = stack.getGroups().stream().filter(g -> g.getInstances().stream().anyMatch(
                inst -> InstanceStatus.CREATE_REQUESTED == inst.getTemplate().getStatus())).collect(Collectors.toList());
        Map<String, Group> groupMap = groups.stream().collect(
                Collectors.toMap(g -> cfStackUtil.getAutoscalingGroupName(ac, cloudFormationClient, g.getName()), g -> g));
        DescribeAutoScalingGroupsResult result = amazonASClient.describeAutoScalingGroups(
                new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(groupMap.keySet()));
        for (AutoScalingGroup asg : result.getAutoScalingGroups()) {
            Group group = groupMap.get(asg.getAutoScalingGroupName());
            List<CloudInstance> groupInstances = group.getInstances().stream().filter(
                    inst -> InstanceStatus.CREATED.equals(inst.getTemplate().getStatus())).collect(Collectors.toList());
            List<CloudVmInstanceStatus> instanceStatuses = instanceConnector.check(ac, groupInstances);
            if (!instanceStatuses.stream().allMatch(inst -> inst.getStatus().equals(InstanceStatus.STARTED))) {
                String errorMessage = "Not all the existing instances are in [Started] state, upscale is not possible!";
                LOGGER.info(errorMessage);
                throw new CloudConnectorException(errorMessage);
            }
            List<Instance> asgOnlyInstances = asg.getInstances().stream()
                    .filter(inst -> groupInstances.stream().noneMatch(gi -> gi.getInstanceId().equals(inst.getInstanceId()))).collect(Collectors.toList());
            List<CloudInstance> cbOnlyInstances = groupInstances.stream()
                    .filter(gi -> asg.getInstances().stream().noneMatch(inst -> inst.getInstanceId().equals(gi.getInstanceId()))).collect(Collectors.toList());
            if (!asgOnlyInstances.isEmpty() || !cbOnlyInstances.isEmpty()) {
                String errorMessage = "The instances in the autoscaling group are not in sync with the instances in cloudbreak! Cloudbreak only instances: ["
                        + cbOnlyInstances.stream().map(CloudInstance::getInstanceId).collect(Collectors.joining(",")) + "], AWS only instances: ["
                        + asgOnlyInstances.stream().map(Instance::getInstanceId).collect(Collectors.joining(",")) + "]. Upscale is not possible!";
                LOGGER.info(errorMessage);
                throw new CloudConnectorException(errorMessage);
            }
            if (groupInstances.size() != asg.getDesiredCapacity()) {
                String errorMessage = String.format("The autoscale group's desired instance count is not match with the instance count in the cloudbreak."
                        + " Desired count: %d <> cb instance count: %d. Upscale is not possible!", asg.getDesiredCapacity(), groupInstances.size());
                LOGGER.info(errorMessage);
                throw new CloudConnectorException(errorMessage);
            }
        }
    }

    @Override
    public void checkQuotas(AuthenticatedContext authenticatedContext, int instancesToCreate, String targetInstanceType) {
        AmazonEC2Client amazonEC2Client = getEC2Client(authenticatedContext);
        AmazonServiceQuotaRetryClient amazonServiceQuotaRetryClient = getServiceQuotasClient(authenticatedContext);
        checkVCPUQuota(instancesToCreate, targetInstanceType, amazonEC2Client, amazonServiceQuotaRetryClient);
    }

    private void checkVCPUQuota(int instancesToCreate, String targetInstanceType, AmazonEC2Client amazonEC2Client, AmazonServiceQuotaRetryClient amazonServiceQuotaRetryClient) {
        Optional<AwsQuotaCodeModel> quotaCodeModel = instanceTypeQuotaCodesConfig.getCodes().stream()
                .filter(awsQuotaCodeModel -> targetInstanceType.matches(awsQuotaCodeModel.getRegex()))
                .findFirst();
        ConcurrentHashMap<String, AtomicInteger> instanceCountMap = collectCountOfRelatedInstances(amazonEC2Client, quotaCodeModel.get().getRegex());
        List<InstanceTypeInfo> instanceTypeInfos = getInstanceTypeInfos(amazonEC2Client, instanceCountMap);
        AtomicInteger currentVCPUUsage = calculateCurrentVCPUUsage(instanceCountMap, instanceTypeInfos);
        Optional<InstanceTypeInfo> targetInstanceTypeInfo = instanceTypeInfos.stream()
                .filter(instanceTypeInfo -> instanceTypeInfo.getInstanceType().contentEquals(targetInstanceType))
                .findFirst();
        if (targetInstanceTypeInfo.isPresent()) {
            int requiredAdditionalVCPU = instancesToCreate * targetInstanceTypeInfo.get().getVCpuInfo().getDefaultVCpus();
            Double limit = getQuota(amazonServiceQuotaRetryClient, quotaCodeModel.get().getCode());
            if (limit < currentVCPUUsage.get() + requiredAdditionalVCPU) {
                throw new CloudConnectorException("Your request would hit the vCPU limit for " + targetInstanceType + " instance type, aborting upscale.");
            }
        } else {
            throw new CloudConnectorException("Not found information about vCPU quota of target instance type " + targetInstanceType + ", aborting upscale.");
        }
    }

    private AtomicInteger calculateCurrentVCPUUsage(ConcurrentHashMap<String, AtomicInteger> instanceCountMap, List<InstanceTypeInfo> instanceTypes) {
        AtomicInteger currentVCPUUsage = new AtomicInteger();
        instanceTypes.stream()
            .forEach(instanceTypeInfo -> {
                currentVCPUUsage.addAndGet(instanceCountMap.get(instanceTypeInfo.getInstanceType()).get() * instanceTypeInfo.getVCpuInfo().getDefaultVCpus());
            });
        return currentVCPUUsage;
    }

    private List<InstanceTypeInfo> getInstanceTypeInfos(AmazonEC2Client amazonEC2Client, ConcurrentHashMap<String, AtomicInteger> instanceCountMap) {
        DescribeInstanceTypesRequest describeInstanceTypesRequest = new DescribeInstanceTypesRequest();
        describeInstanceTypesRequest.setInstanceTypes(instanceCountMap.keySet());
        return amazonEC2Client.describeInstanceTypes(describeInstanceTypesRequest).getInstanceTypes();
    }

    private ConcurrentHashMap<String, AtomicInteger> collectCountOfRelatedInstances(AmazonEC2Client amazonEC2Client, String regex) {
        ConcurrentHashMap<String, AtomicInteger> instanceCountMap = new ConcurrentHashMap<String, AtomicInteger>();
        amazonEC2Client.describeInstances(new DescribeInstancesRequest()).getReservations().stream()
                .map(rs -> rs.getInstances())
                .flatMap(List::stream)
                .filter(instance -> instance.getInstanceType().matches(regex))
                .forEach(instance -> {
                    instanceCountMap.putIfAbsent(instance.getInstanceType(), new AtomicInteger());
                    instanceCountMap.get(instance.getInstanceType()).incrementAndGet();
                });
        return instanceCountMap;
    }

    private Double getQuota(AmazonServiceQuotaRetryClient amazonServiceQuotaRetryClient, String quotaCode) {
        GetServiceQuotaRequest getServiceQuotaRequest = new GetServiceQuotaRequest();
        getServiceQuotaRequest.setServiceCode("ec2");
        getServiceQuotaRequest.setQuotaCode(quotaCode);
        return amazonServiceQuotaRetryClient.getServiceQuotaResult(getServiceQuotaRequest).getQuota().getValue();
    }

    private AmazonEC2Client getEC2Client(AuthenticatedContext ac) {
        return awsClient.createAccess(new AwsCredentialView(ac.getCloudCredential()),
                ac.getCloudContext().getLocation().getRegion().value());
    }

    private AmazonServiceQuotaRetryClient getServiceQuotasClient(AuthenticatedContext ac) {
        return awsClient.createServiceQuotasRetryClient(new AwsCredentialView(ac.getCloudCredential()),
                ac.getCloudContext().getLocation().getRegion().value());
    }
}
