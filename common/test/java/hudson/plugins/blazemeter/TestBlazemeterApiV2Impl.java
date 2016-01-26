package hudson.plugins.blazemeter;

import com.blaze.APIFactory;
import com.blaze.ApiVersion;
import com.blaze.api.BlazemeterApiV2Impl;
import com.blaze.runner.Constants;
import com.blaze.runner.TestStatus;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBlazemeterApiV2Impl {
    BlazemeterApiV2Impl blazemeterApiV2 =null;

    @Before
    public void setUp(){
    blazemeterApiV2=(BlazemeterApiV2Impl) APIFactory.getAPI(null, TestConstants.mockedApiUrl,ApiVersion.v2.name());
    }

    @Test
    public void createTest(){
        Assert.assertEquals(blazemeterApiV2.createTest(null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void retrieveJUNITXML(){
        Assert.assertEquals(blazemeterApiV2.retrieveJUNITXML(null), Constants.NOT_IMPLEMENTED);
    }


    @Test
    public void getCIStatus(){
        try {
            Assert.assertEquals(blazemeterApiV2.getCIStatus(null), BlazemeterApiV2Impl.not_implemented);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateTestInfo(){
        Assert.assertEquals(blazemeterApiV2.postJsonConfig(null, null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void getTestStatus(){
        Assert.assertEquals(blazemeterApiV2.getTestStatus(null), BlazemeterApiV2Impl.not_implemented);
    }

    @Test
    public void getUser(){
        Assert.assertEquals(blazemeterApiV2.getUser(), null);
    }
 
    @Test
    public void getTestList(){
        try {
            Assert.assertEquals(blazemeterApiV2.getTestList(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

   @Test
    public void stopTest(){
       try {
           Assert.assertEquals(blazemeterApiV2.stopTest(null), null);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   @Test
    public void startTest(){
       try {
           Assert.assertEquals(blazemeterApiV2.startTest(null,null), null);
       } catch (JSONException e) {
           e.printStackTrace();
       }
   }


   @Test
    public void getTestRunStatus(){
        Assert.assertEquals(blazemeterApiV2.getTestStatus(null), TestStatus.NotFound);
    }

}