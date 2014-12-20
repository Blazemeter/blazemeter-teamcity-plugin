package com.blaze;

import com.blaze.api.BlazemeterApi;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public class Utils {

    private Utils(){}


    public static String getFileContents(String fn) {

        // ...checks on aFile are elided
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);

        try {

            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));

            try {
                String line;    // not declared within while loop

                /*
                 *         readLine is a bit quirky : it returns the content of a line
                 *         MINUS the newline. it returns null only for the END of the
                 *         stream. it returns an empty String if two newlines appear in
                 *         a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }

        return contents.toString();
    }

    public static void updateTest(String apiKey,BlazemeterApi api, String testId, int updDuration, BuildProgressLogger logger) {
        try {
            JSONObject jo = api.getTestInfo(apiKey,testId,logger);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            override.put("duration", String.valueOf(updDuration));
            api.putTestInfo(apiKey,testId, result,logger);

        } catch (JSONException je) {
            logger.message("Received JSONException while saving testDuration: "+ je);
        } catch (Exception e) {
            logger.message("Received JSONException while saving testDuration: "+ e);
        }
    }
}
