package com.blaze.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testVersion() throws Exception {
        String version = Utils.version();
        System.out.println(version);
        assertNotNull(version);
        assertFalse("N/A".equals(version));
    }

    @Test
    public void testGetTestId() throws Exception {
        assertEquals("123456", Utils.getTestId("123456.http"));
        assertEquals("123456http", Utils.getTestId("123456http"));
    }
}