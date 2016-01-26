package hudson.plugins.blazemeter;

import com.blaze.APIFactory;
import com.blaze.ApiVersion;
import com.blaze.api.*;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAPIFactory {
    private BlazemeterApi blazemeterApi = null;
    private String userKey1 = "1234567890";
    private String userKey2 = "0987654321";


    @Test
    public void testMixVersion() {
        blazemeterApi = APIFactory.getAPI(userKey1, TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV3Impl);
        blazemeterApi = APIFactory.getAPI(userKey1, TestConstants.mockedApiUrl,ApiVersion.v2.name());
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV2Impl);
        blazemeterApi = APIFactory.getAPI(userKey1, TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV3Impl);

    }
}