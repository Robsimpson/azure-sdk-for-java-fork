// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.compute.batch;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.compute.batch.models.*;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import reactor.core.publisher.Mono;

import java.util.*;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.core.credential.TokenCredential;

import org.junit.Assert;


class BatchClientTestBase extends TestProxyTestBase {
    protected BatchClientBuilder batchClientBuilder;

    protected BatchClient batchClient;

    static final int MAX_LEN_ID = 64;

    static String redacted = "REDACTED";

    public enum AuthMode {
        AAD, SharedKey
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        batchClientBuilder =
            new BatchClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_BATCH_ENDPOINT", "https://fakeaccount.batch.windows.net"))
                .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            batchClientBuilder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));

            addTestRulesOnPlayback(interceptorManager);
        } else if (getTestMode() == TestMode.RECORD) {
            batchClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
            addTestSanitizersAndRules(interceptorManager);
        }

        authenticateClient(AuthMode.AAD);

        batchClient = batchClientBuilder.buildClient();
    }

    public void addTestRulesOnPlayback(InterceptorManager interceptorManager) {
        List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
        customMatchers.add(new CustomMatcher().setComparingBodies(false));
        customMatchers.add(new CustomMatcher().setExcludedHeaders(Arrays.asList("ocp-date", "client-request-id")));
        interceptorManager.addMatchers(customMatchers);
    }

    public void addTestSanitizersAndRules(InterceptorManager interceptorManager) {
        List<TestProxySanitizer> testProxySanitizers = new ArrayList<TestProxySanitizer>();
        testProxySanitizers.add(new TestProxySanitizer("$..httpUrl", null, redacted, TestProxySanitizerType.BODY_KEY));
        testProxySanitizers.add(new TestProxySanitizer("$..containerUrl", null, redacted, TestProxySanitizerType.BODY_KEY));
        interceptorManager.addSanitizers(testProxySanitizers);
    }

    void authenticateClient(AuthMode auth) {
        if (getTestMode() == TestMode.RECORD) {
            if (auth == AuthMode.AAD) {
                batchClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
            } else {
                AzureNamedKeyCredential keyCredentials = getSharedKeyCredentials();
                batchClientBuilder.credential(keyCredentials);
            }
        }
    }

    static AzureNamedKeyCredential getSharedKeyCredentials() {
        Configuration localConfig = Configuration.getGlobalConfiguration();
        String accountName = localConfig.get("AZURE_BATCH_ACCOUNT", "fakeaccount");
        String accountKey = localConfig.get("AZURE_BATCH_ACCESS_KEY", "fakekey");
        return new AzureNamedKeyCredential(accountName, accountKey);
    }

    static String getStringIdWithUserNamePrefix(String name) {
        //'BatchUser' is the name used for Recording / Playing Back tests.
        // For Local testing, use your username here, to create your unique Batch resources and avoiding conflict in shared batch account.
        String userName = "BatchUser";
        StringBuilder out = new StringBuilder();
        int remainingSpace = MAX_LEN_ID - name.length();
        if (remainingSpace > 0) {
            if (userName.length() > remainingSpace) {
                out.append(userName.substring(0, remainingSpace));
            } else {
                out.append(userName);
            }
            out.append(name);
        } else {
            out.append(name.substring(0, MAX_LEN_ID));
        }
        return out.toString();
    }

    BatchPool createIfNotExistIaaSPool(String poolId) throws Exception {
        // Create a pool with 3 Small VMs
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 1;

        // 10 minutes
        long poolSteadyTimeoutInSeconds = 10 * 60 * 1000;

        // Check if pool exists
        if (!poolExists(batchClient, poolId)) {
            // Use IaaS VM with Ubuntu
            ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                .setSku("18.04-LTS").setVersion("latest");

            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

            List<UserAccount> userList = new ArrayList<>();
            userList.add(new UserAccount("test-user", "kt#_gahr!@aGERDXA")
                .setLinuxUserConfiguration(new LinuxUserConfiguration().setUid(5).setGid(5))
                .setElevationLevel(ElevationLevel.ADMIN));

            // Need VNet to allow security to inject NSGs
            NetworkConfiguration networkConfiguration = createNetworkConfiguration();

            BatchPoolCreateContent poolToCreate = new BatchPoolCreateContent(poolId, poolVmSize);
            poolToCreate.setTargetDedicatedNodes(poolVmCount)
                .setVirtualMachineConfiguration(configuration)
                .setUserAccounts(userList)
                .setNetworkConfiguration(networkConfiguration);

            batchClient.createPool(poolToCreate);
        } else {
            System.out.println(String.format("The %s already exists.", poolId));
            //logger.log(createLogRecord(Level.INFO, String.format("The %s already exists.", poolId)));
        }


        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean steady = false;
        BatchPool pool = null;

        // Wait for the VM to be allocated
        while (elapsedTime < poolSteadyTimeoutInSeconds) {
            pool = batchClient.getPool(poolId);
            if (pool.getAllocationState() == AllocationState.STEADY) {
                steady = true;
                break;
            }
            System.out.println("wait 30 seconds for pool steady...");
            Thread.sleep(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);

        return pool;
    }

    NetworkConfiguration createNetworkConfiguration() {
        Configuration localConfig = Configuration.getGlobalConfiguration();
        String vnetName = localConfig.get("AZURE_VNET", "");
        String subnetName = localConfig.get("AZURE_VNET_SUBNET");
        String subId = localConfig.get("AZURE_SUBSCRIPTION_ID");
        String vnetResourceGroup = localConfig.get("AZURE_VNET_RESOURCE_GROUP");

        if (getTestMode() == TestMode.RECORD) {
            AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            NetworkManager manager = NetworkManager
                .authenticate(credential, profile);

            PagedIterable<Network> networks = manager.networks().listByResourceGroup(vnetResourceGroup);
            boolean networksFound = false;

            for (Network network : networks) {
                networksFound = true;
                break;
            }

            if (!networksFound) {
                Network network = manager.networks().define(vnetName)
                    .withRegion(localConfig.get("AZURE_BATCH_REGION"))
                    .withExistingResourceGroup(vnetResourceGroup)
                    .withAddressSpace(localConfig.get("AZURE_VNET_ADDRESS_SPACE"))
                    .withSubnet(subnetName, localConfig.get("AZURE_VNET_SUBNET_ADDRESS_SPACE"))
                    .create();
            }
        }

        String vNetResourceId = String.format(
            "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/virtualNetworks/%s/subnets/%s",
            subId,
            vnetResourceGroup,
            vnetName,
            subnetName);

        return new NetworkConfiguration().setSubnetId(vNetResourceId);
    }

    void threadSleepInRecordMode(long millis) throws InterruptedException {
        // Called for long timeouts which should only happen in Record mode.
        // Speeds up the tests in Playback mode.
        if (getTestMode() == TestMode.RECORD) {
            Thread.sleep(millis);
        }
    }

    static boolean poolExists(BatchClient batchClient, String poolId) {
        return batchClient.poolExists(poolId);
    }

    static BlobContainerClient createBlobContainer(String storageAccountName, String storageAccountKey,
                                                   String containerName) throws BlobStorageException {
        // Create storage credential from name and key
        String endPoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", storageAccountName);
        StorageSharedKeyCredential credentials = new StorageSharedKeyCredential(storageAccountName, storageAccountKey);
        BlobContainerClient containerClient = new BlobContainerClientBuilder().credential(credentials).containerName(containerName).endpoint(endPoint).buildClient();

        return containerClient;
    }

    /**
     * Upload file to blob container and return sas key
     *
     * @param container blob container
     * @param fileName  the file name of blob
     * @param filePath  the local file path
     * @return SAS key for the uploaded file
     */
    static String uploadFileToCloud(BlobContainerClient container, String fileName, String filePath)
        throws BlobStorageException {
        // Create the container if it does not exist.
        container.createIfNotExists();
        BlobClient blobClient = container.getBlobClient(fileName);

        //Upload file
        File source = new File(filePath);
        blobClient.uploadFromFile(source.getPath());

        // Create policy with 1 day read permission
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true);

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);

        // Create SAS key
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sas = blobClient.generateSas(sasSignatureValues);
        return blobClient.getBlobUrl() + "?" + sas;
    }

    static String generateContainerSasToken(BlobContainerClient container) throws InvalidKeyException {
        container.createIfNotExists();

        // Create policy with 1 day read permission
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true);

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);

        // Create SAS key
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sas = container.generateSas(sasSignatureValues);
        return container.getBlobContainerUrl() + "?" + sas;
    }

    static boolean waitForTasksToComplete(BatchClient batchClient, String jobId, int expiryTimeInSeconds)
        throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        while (elapsedTime < expiryTimeInSeconds * 1000) {

            ListBatchTasksOptions options = new ListBatchTasksOptions();
            options.setSelect(Arrays.asList("id", "state"));
            PagedIterable<BatchTask> taskIterator = batchClient.listTasks(jobId, options);

            boolean allComplete = true;
            for (BatchTask task : taskIterator) {
                if (task.getState() != BatchTaskState.COMPLETED) {
                    allComplete = false;
                    break;
                }
            }

            if (allComplete) {
                // All tasks completed
                return true;
            }

            // Check again after 10 seconds
            Thread.sleep(10 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        // Timeout, return false
        return false;
    }

    BatchPool waitForPoolState(String poolId, AllocationState targetState, long poolAllocationTimeoutInMilliseconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean allocationStateReached = false;
        BatchPool pool = null;

        // Wait for the VM to be allocated
        while (elapsedTime < poolAllocationTimeoutInMilliseconds) {
            pool = batchClient.getPool(poolId);
            Assert.assertNotNull(pool);

            if (pool.getAllocationState() == targetState) {
                allocationStateReached = true;
                break;
            }

            System.out.println("wait 30 seconds for pool allocationStateReached...");
            threadSleepInRecordMode(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a allocationStateReached state in the allotted time", allocationStateReached);
        return pool;
    }

    static String getContentFromContainer(BlobContainerClient container, String fileName)
        throws URISyntaxException, IOException {
        BlobClient blobClient = container.getBlobClient(fileName);
        return blobClient.downloadContent().toString();
    }
}
