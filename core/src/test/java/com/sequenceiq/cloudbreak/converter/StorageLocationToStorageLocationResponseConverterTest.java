package com.sequenceiq.cloudbreak.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.StorageLocationV4;
import com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.filesystem.StorageLocationToStorageLocationV4Converter;
import com.sequenceiq.cloudbreak.domain.StorageLocation;

public class StorageLocationToStorageLocationResponseConverterTest {

    private static final String PROPERTY_FILE = "/some/file";

    private static final String PROPERTY_NAME = "path";

    private static final String VALUE = "propertyValue";

    private StorageLocationToStorageLocationV4Converter underTest;

    @Before
    public void setUp() {
        underTest = new StorageLocationToStorageLocationV4Converter();
    }

    @Test
    public void testConvertWhenPassingStorageLocationThenAllNecessaryParametersShouldBePassed() {
        StorageLocationV4 expected = new StorageLocationV4();
        expected.setValue(VALUE);
        expected.setPropertyName(PROPERTY_NAME);
        expected.setPropertyFile(PROPERTY_FILE);

        StorageLocationV4 result = underTest.convert(createSource());

        assertEquals(expected, result);
    }

    private StorageLocation createSource() {
        StorageLocation response = new StorageLocation();
        response.setConfigFile(PROPERTY_FILE);
        response.setProperty(PROPERTY_NAME);
        response.setValue(VALUE);
        return response;
    }

}