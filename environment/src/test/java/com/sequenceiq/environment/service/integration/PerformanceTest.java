package com.sequenceiq.environment.service.integration;

import org.jsmart.zerocode.core.domain.LoadWith;
import org.jsmart.zerocode.core.domain.TestMapping;
import org.jsmart.zerocode.core.domain.TestMappings;
import org.jsmart.zerocode.jupiter.extension.ParallelLoadExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@LoadWith("load_generation.properties")
@ExtendWith({ParallelLoadExtension.class})
public class PerformanceTest {

    @Test
    @DisplayName("testing parallel load for X, Y and Z scenarios")
    @TestMappings({
            @TestMapping(testClass = EnvironmentServiceIntegrationTest.class, testMethod = "testProxyCreate")})
    public void testLoad_xyz() {
        // This space remains empty
    }
}
