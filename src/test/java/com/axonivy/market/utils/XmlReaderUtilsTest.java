package com.axonivy.market.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class XmlReaderUtilsTest {


    @Test
    void testExtractVersions() {
        List<String> versions = Collections.emptyList();
        XmlReaderUtils.extractVersions(StringUtils.EMPTY, versions);
        Assertions.assertTrue(versions.isEmpty());
    }

}
