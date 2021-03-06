
/**
 * This template file was generated by Dynatrace client.
 * The Dynatrace community portal can be found here: http://community.dynatrace.com/
 * For information how to publish a plugin please visit https://community.dynatrace.com/community/display/DL/How+to+add+a+new+plugin/
 **/

package com.dynatrace.opsgenie;

import com.dynatrace.diagnostics.pdk.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


public class OpsGeniePlugin implements ActionV2 {

    private static final Logger logger = Logger.getLogger(OpsGeniePlugin .class.getName());

    private final static String TEAMS = "teams";
    private final static String URL = "url";

    private final static String KEY = "key";
    private final static String STATE = "state";
    private final static String DURATION = "duration";
    private final static String STARTTIME = "startTime";
    private final static String ENDTIME = "endTime";
    private final static String ACTION = "action";
    private final static String MESSAGE = "message";
    private final static String SEVERITY = "severity";
    private final static String SERVER_NAME = "serverName";
    private final static String INCIDENT_RULE_NAME = "incidentRuleName";
    private final static String INCIDENT_RULE_DESCRIPTION = "incidentRuleDesc";
    private final static String VIOLATIONS = "violations";
    private final static String MEASURE_APPLICATION = "measureApp";
    private final static String MEASURE_CONF_SUMMARY = "measureConfSummary";
    private final static String MEASURE_DESC = "measureDesc";
    private final static String MEASURE_NAME = "measureName";
    private final static String MEASURE_UNIT = "measureUnit";
    private final static String THRESHOLD_TYPE = "threaholdType";
    private final static String THRESHOLD_VALUE = "thresholdValue";

    private String teams;
    private URL url;


    /**
     * Initializes the Plugin. This method is called in the following cases:
     * <ul>
     * <li>before <tt>execute</tt> is called the first time for this
     * scheduled Plugin</li>
     * <li>before the next <tt>execute</tt> if <tt>teardown</tt> was called
     * after the last execution</li>
     * </ul>
     *
     * <p>
     * If the returned status is <tt>null</tt> or the status code is a
     * non-success code then {@link #teardown(ActionEnvironment)} will be called
     * next.
     *
     * <p>
     * Resources like sockets or files can be opened in this method.
     * Resources like sockets or files can be opened in this method.
     * @param env
     *            the configured <tt>ActionEnvironment</tt> for this Plugin
     * @see #teardown(ActionEnvironment)
     * @return a <tt>Status</tt> object that describes the result of the
     *         method call
     */
    @Override
    public Status setup(ActionEnvironment env) throws Exception {
        teams = env.getConfigString(TEAMS);
        url = env.getConfigUrl(URL);

        return new Status(Status.StatusCode.Success);
    }

    /**
     * Executes the Action Plugin to process incidents.
     *
     * <p>
     * This method may be called at the scheduled intervals, but only if incidents
     * occurred in the meantime. If the Plugin execution takes longer than the
     * schedule interval, subsequent calls to
     * {@link #execute(ActionEnvironment)} will be skipped until this method
     * returns. After the execution duration exceeds the schedule timeout,
     * {@link ActionEnvironment#isStopped()} will return <tt>true</tt>. In this
     * case execution should be stopped as soon as possible. If the Plugin
     * ignores {@link ActionEnvironment#isStopped()} or fails to stop execution in
     * a reasonable timeframe, the execution thread will be stopped ungracefully
     * which might lead to resource leaks!
     *
     * @param env
     *            a <tt>ActionEnvironment</tt> object that contains the Plugin
     *            configuration and incidents
     * @return a <tt>Status</tt> object that describes the result of the
     *         method call
     */
    @Override
    public Status execute(ActionEnvironment env) throws Exception {

        Status.StatusCode code = Status.StatusCode.Success;
        Collection<Incident> incidents = env.getIncidents();
        for (Incident incident : incidents) {

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(5000);
            con.setReadTimeout(20000);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put(TEAMS, teams);

            Key keyObj = incident.getKey();
            if (keyObj != null) {
                String key = keyObj.getUUID();
                jsonObj.put(KEY, key);
            }

            String message = incident.getMessage();
            logger.info("Incident " + message + " triggered.");
            jsonObj.put(MESSAGE, message);

            int state = incident.getState();
            jsonObj.put(STATE, state);

            Duration durationObj = incident.getDuration();
            if (durationObj != null) {
                long duration = durationObj.getDurationInMs();
                jsonObj.put(DURATION, duration);
            }

            Timestamp startTimestamp = incident.getStartTime();
            if (startTimestamp != null) {
                long startTime = startTimestamp.getTimestampInMs();
                jsonObj.put(STARTTIME, startTime);
            }

            Timestamp endTimestamp = incident.getEndTime();
            if (endTimestamp != null) {
                long endTime = endTimestamp.getTimestampInMs();
                jsonObj.put(ENDTIME, endTime);
            }


            String severity = incident.getSeverity().toString();
            jsonObj.put(SEVERITY, severity);

            String serverName = incident.getServerName();
            jsonObj.put(SERVER_NAME, serverName);

            IncidentRule incidentRule = incident.getIncidentRule();
            if (incidentRule != null) {
                String incidentRuleName = incidentRule.getName();
                jsonObj.put(INCIDENT_RULE_NAME, incidentRuleName);

                String incidentRuleDesc = incidentRule.getDescription();
                jsonObj.put(INCIDENT_RULE_DESCRIPTION, incidentRuleDesc);
            }

            if (incident.isClosed()) {
                jsonObj.put(ACTION, "close");
            } else {
                jsonObj.put(ACTION, "create");
            }
            List violations = new ArrayList<>();
            for (Violation violation : incident.getViolations()) {
                Map violationMap = new HashMap<>();
                if (violation != null) {
                    Threshold threshold = violation.getViolatedThreshold();
                    if (threshold != null) {
                        String thresholdType = threshold.getType().toString();
                        Value value = threshold.getValue();
                        if (value != null) {
                            double thresholdValue = value.getValue();
                            violationMap.put(THRESHOLD_VALUE, thresholdValue);
                        }
                        violationMap.put(THRESHOLD_TYPE, thresholdType);

                    }
                    Measure measure = violation.getViolatedMeasure();
                    if (measure != null) {
                        String measureName = measure.getName();
                        String measureDesc = measure.getDescription();
                        String measureConfSummary = measure.getConfigurationSummary();
                        String measureApplication = measure.getApplication();
                        String measureUnit = measure.getUnit();

                        violationMap.put(MEASURE_NAME, measureName);
                        violationMap.put(MEASURE_DESC, measureDesc);
                        violationMap.put(MEASURE_CONF_SUMMARY, measureConfSummary);
                        violationMap.put(MEASURE_APPLICATION, measureApplication);
                        violationMap.put(MEASURE_UNIT, measureUnit);
                    }

                    violations.add(violationMap);
                }

            }
            jsonObj.put(VIOLATIONS, violations);

            String jsonStr = jsonObj.toJSONString();

            byte[] payload = jsonStr.getBytes();

            con.setFixedLengthStreamingMode(payload.length);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);

            try {
                logger.log(Level.FINE, "Sending to OpsGenie : %s", jsonStr);

                OutputStream channel = con.getOutputStream();
                channel.write(payload);
                channel.close();

                int responseCode = con.getResponseCode();
                if (responseCode != 200) {
                    code = Status.StatusCode.PartialSuccess;
                }

                logger.log(Level.FINE, "Response Code : %d", responseCode);
                logger.log(Level.FINE, "Response Body : %s", con.getResponseMessage());
            } catch (IOException e) {
                code = Status.StatusCode.ErrorInternalException;
                logger.log(Level.SEVERE, "Exception thrown while writing to output stream...", e);
            } finally {
                con.disconnect();
            }


        }

        return new Status(code);
    }

    /**
     * Shuts the Plugin down and frees resources. This method is called in the
     * following cases:
     * <ul>
     * <li>the <tt>setup</tt> method failed</li>
     * <li>the Plugin configuration has changed</li>
     * <li>the execution duration of the Plugin exceeded the schedule timeout</li>
     * <li>the schedule associated with this Plugin was removed</li>
     * </ul>
     * <p>
     * The Plugin methods <tt>setup</tt>, <tt>execute</tt> and
     * <tt>teardown</tt> are called on different threads, but they are called
     * sequentially. This means that the execution of these methods does not
     * overlap, they are executed one after the other.
     *
     * <p>
     * Examples:
     * <ul>
     * <li><tt>setup</tt> (failed) -&gt; <tt>teardown</tt></li>
     * <li><tt>execute</tt> starts, configuration changes, <tt>execute</tt>
     * ends -&gt; <tt>teardown</tt><br>
     * on next schedule interval: <tt>setup</tt> -&gt; <tt>execute</tt> ...</li>
     * <li><tt>execute</tt> starts, execution duration timeout,
     * <tt>execute</tt> stops -&gt; <tt>teardown</tt></li>
     * <li><tt>execute</tt> starts, <tt>execute</tt> ends, schedule is
     * removed -&gt; <tt>teardown</tt></li>
     * </ul>
     * Failed means that either an unhandled exception is thrown or the status
     * returned by the method contains a non-success code.
     *
     * <p>
     * All by the Plugin allocated resources should be freed in this method.
     * Examples are opened sockets or files.
     *
     * @see #setup(ActionEnvironment)
     */
    @Override
    public void teardown(ActionEnvironment env) throws Exception {
    }
}