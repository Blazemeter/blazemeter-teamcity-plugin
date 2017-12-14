package com.blaze.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class TCBzmUtilsTest {

    @Test
    public void testFlow() throws Exception {
        TCBzmUtils utils = new TCBzmUtils("id", "secret", "address", null, null);

        String url = utils.modifyRequestUrl("http://blazedemo.com/");
        assertTrue(url, url.contains("?app_key=jnk100x987c06f4e10c4&_clientId=CI_TEAMCITY&_clientVersion="));

        url = utils.modifyRequestUrl("http://blazedemo.com/?param=val");
        assertTrue(url, url.contains("&app_key=jnk100x987c06f4e10c4&_clientId=CI_TEAMCITY&_clientVersion="));
    }
}