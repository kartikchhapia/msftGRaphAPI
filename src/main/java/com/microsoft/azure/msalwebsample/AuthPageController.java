// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.msalwebsample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller exposing application endpoints
 */
@Controller
public class AuthPageController {

    @Autowired
    AuthHelper authHelper;

    @RequestMapping("/msal4jsample")
    public String homepage() {
        return "index";
    }

    @RequestMapping("/msal4jsample/secure/aad")
    public ModelAndView securePage(HttpServletRequest httpRequest) throws ParseException {
        ModelAndView mav = new ModelAndView("auth_page");

        setAccountInfo(mav, httpRequest);

        return mav;
    }

    @RequestMapping("/msal4jsample/sign_out")
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {

        httpRequest.getSession().invalidate();

        String endSessionEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/logout";

        String redirectUrl = "https://localhost:8443/msal4jsample/";
        response.sendRedirect(endSessionEndpoint + "?post_logout_redirect_uri=" +
                URLEncoder.encode(redirectUrl, "UTF-8"));
    }

    @RequestMapping("/msal4jsample/graph/me")
    public ModelAndView getUserFromGraph(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws Throwable {

        IAuthenticationResult result;
        ModelAndView mav;
        try {
            result = authHelper.getAuthResultBySilentFlow(httpRequest, httpResponse);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MsalInteractionRequiredException) {

                // If silent call returns MsalInteractionRequired, then redirect to Authorization endpoint
                // so user can consent to new scopes
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);
                String authorizationCodeUrl = authHelper.getAuthorizationCodeUrl(
                        httpRequest.getParameter("claims"),
                        "User.Read",
                        authHelper.getRedirectUriGraph(),
                        state,
                        nonce);

                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {

                mav = new ModelAndView("error");
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            mav = new ModelAndView("auth_page");
            setAccountInfo(mav, httpRequest);

            try {
                mav.addObject("userInfo", getUserInfoFromGraph(result.accessToken()));

                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    @RequestMapping("/respondNotification")
    public ResponseEntity respondNotifaction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        System.out.println("printing response for respondNotification: "+ResponseEntity.ok().build());
        return ResponseEntity.ok().build(); //OR ResponseEntity.ok("body goes heare");
    }

    @RequestMapping("/build-notif")
    public ModelAndView buildNotif(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        System.out.println("inside build notif "+httpRequest);


        IAuthenticationResult result;
        ModelAndView mav;
        try {
            result = authHelper.getAuthResultBySilentFlow(httpRequest, httpResponse);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MsalInteractionRequiredException) {

                // If silent call returns MsalInteractionRequired, then redirect to Authorization endpoint
                // so user can consent to new scopes
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);
                String authorizationCodeUrl = authHelper.getAuthorizationCodeUrl(
                        httpRequest.getParameter("claims"),
                        "User.Read",
                        authHelper.getRedirectUriGraph(),
                        state,
                        nonce);

                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {

                mav = new ModelAndView("error");
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            mav = new ModelAndView("auth_page");
            setAccountInfo(mav, httpRequest);

            try {
                mav.addObject("notifStuff", notificationstuff(result.accessToken()));

                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;










    }

    private String notificationstuff(String accessToken) throws IOException {
        System.out.println("inside notificationstuff method access token is "+accessToken);



        URL url = new URL ("https://graph.microsoft.com/v1.0/communications/callRecords/35605ea9-ddff-4431-8f39-ea41852f5784");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();


        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);
        con.setRequestProperty("Accept", "application/json");


        Map<String,Object> params = new LinkedHashMap<>();
        params.put("changeType", "created,updated");
        params.put("notificationUrl", "https://www.google.com");
        params.put("resource", "/communications/callRecords");
        params.put("expirationDateTime", "2022-03-20T11:00:00.0000000Z");
        params.put("clientState", "SecretClientState");

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        con.setDoOutput(true);
        con.getOutputStream().write(postDataBytes);



        //con.setDoOutput(true);
        //String jsonInputString = "{\"name\": \"Upendra\", \"job\": \"Programmer\"}";
//        String jsonInputString = "{ \"changeType\": \"created,updated\", \"notificationUrl\": \"https://webhook.azurewebsites.net/notificationClient\", \"resource\": \"/me/mailfolders('inbox')/messages\", \"expirationDateTime\": \"2016-03-20T11:00:00.0000000Z\", \"clientState\": \"SecretClientState\" }";
//        jsonInputString= "";
//
//        try(OutputStream os = con.getOutputStream()) {
//            byte[] input = jsonInputString.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("got a response"+response);
            System.out.println(response.toString());
            return response.toString();
        }


    }


    @RequestMapping("/notif")
    public ModelAndView notificationURL(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        System.out.println("inside notification URL, request is "+httpRequest);
        return new ModelAndView();

    }

    @RequestMapping("/msal4jsample/graph/messages")
    public ModelAndView getUserEmails(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws Throwable {

        IAuthenticationResult result;
        ModelAndView mav;
        try {
            result = authHelper.getAuthResultBySilentFlow(httpRequest, httpResponse);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MsalInteractionRequiredException) {

                // If silent call returns MsalInteractionRequired, then redirect to Authorization endpoint
                // so user can consent to new scopes
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);
                String authorizationCodeUrl = authHelper.getAuthorizationCodeUrl(
                        httpRequest.getParameter("claims"),
                        "User.Read",
                        authHelper.getRedirectUriGraph(),
                        state,
                        nonce);

                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {

                mav = new ModelAndView("error");
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            mav = new ModelAndView("auth_page");
            setAccountInfo(mav, httpRequest);

            try {
                mav.addObject("emailInfo", getUserEmailsFromGraph(result.accessToken()));

                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    private List<Email> getUserEmailsFromGraph(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        System.out.println("calling email now");


        JSONObject responseObject = makeHttpCalendarCall(authHelper.getMsGraphEndpointHost() + "v1.0/me/messages", accessToken);
        //JSONObject responseObject = makeHttpCalendarCall(authHelper.getMsGraphEndpointHost() + "v1.0/me/joinedTeams", accessToken);

        boolean pagination = checkPagination(responseObject);
        //System.out.println("responseObject: "+responseObject);
        //System.out.println("Pagination value first time: "+pagination);
        HashMap<String, TreeMap<String, Integer>> emailMap = new HashMap<>();
        procesEmailsLink(responseObject, emailMap);
        //buildMap(responseObject, usersInteractionMap);
        String nextUrl = pagination == true ? ((JSONObject) responseObject.get("responseMsg")).getString("@odata.nextLink")  : "";

        while (pagination) {
            //System.out.println("next url is: "+nextUrl);
            responseObject = makeHttpCalendarCall(nextUrl, accessToken);
            //System.out.println("http call done");
            pagination = checkPagination(responseObject);
            //System.out.println("Pagination value in while loop : "+pagination);
            procesEmailsLink(responseObject, emailMap);

            nextUrl = pagination == true ? ((JSONObject) responseObject.get("responseMsg")).getString("@odata.nextLink")  : "";

        }

        List<Email> emailList = new ArrayList<>();
        for (String fromEmail: emailMap.keySet()) {
            for (String toEmail : emailMap.get(fromEmail).keySet()) {
                Email email = new Email(fromEmail, toEmail, emailMap.get(fromEmail).get(toEmail));
                emailList.add(email);
            }

        }


        return emailList;
    }

    private void procesEmailsLink(JSONObject responseObject, HashMap<String, TreeMap<String, Integer>> emailMap) throws JSONException {
        JSONArray tsmresponse = (JSONArray) (((JSONObject) responseObject.get("responseMsg")).get("value"));
        System.out.println("tsmresponse: "+tsmresponse);
        List<String> toEmailList = new ArrayList<>();
        List<String> fromEmailList = new ArrayList<>();


        for(int i=0; i<tsmresponse.length(); i++) {
            String fromEmailAddress =  tsmresponse.getJSONObject(i).getJSONObject("from").getJSONObject("emailAddress").getString("address");
            JSONArray toEmailLJson = (JSONArray) tsmresponse.getJSONObject(i).get("toRecipients");
            //System.out.println("fromEmailAddress: "+fromEmailAddress);
            for (int j = 0 ; j < toEmailLJson.length(); j++) {
                String toEmailAddress = toEmailLJson.getJSONObject(j).getJSONObject("emailAddress").getString("address");
                //System.out.println("To email address: "+toEmailAddress);
                fromEmailList.add(toEmailAddress);
                if (!emailMap.containsKey(fromEmailAddress)){
                    emailMap.put(fromEmailAddress, new TreeMap<String, Integer>(Comparator.reverseOrder()));
                }
                emailMap.get(fromEmailAddress).put(toEmailAddress, emailMap.get(fromEmailAddress).getOrDefault(toEmailAddress, 0)+1);
            }
        }





    }

    private List<Person> getUserInfoFromGraph(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        System.out.println("calling calendar now");


        JSONObject responseObject = null;
        try {
            //JSONObject responseObject = makeHttpCalendarCall(authHelper.getMsGraphEndpointHost() + "v1.0/communications/callRecords/7744dca6-703e-4880-bbd0-3d17e4932b83", accessToken);
             responseObject =  makeHttpCalendarCall(authHelper.getMsGraphEndpointHost() + "v1.0/me/calendar/events", accessToken);
             //communications/callRecords/35605ea9-ddff-4431-8f39-ea41852f5784

        }
        catch (Exception e) {
            System.out.println(e);
        }
        //JSONObject responseObject = makeHttpCalendarCall(authHelper.getMsGraphEndpointHost() + "v1.0/me/calendar/events", accessToken);


        //JSONObject responseObject = makeHttpCalendarCall(authHelper.getMsGraphEndpointHost() + "v1.0/groups/9ac30032-2385-4345-8bf2-2a6185c36c16", accessToken);

        boolean pagination = checkPagination(responseObject);
        System.out.println("Pagination value first time: "+pagination);
        HashMap<String, HashMap<String, Object>> usersInteractionMap = new HashMap<>();
        buildMap(responseObject, usersInteractionMap);
        String nextUrl = pagination == true ? ((JSONObject) responseObject.get("responseMsg")).getString("@odata.nextLink")  : "";

        while (pagination) {
            System.out.println("next url is: "+nextUrl);
            responseObject = makeHttpCalendarCall(nextUrl, accessToken);
            System.out.println("http call done");
            pagination = checkPagination(responseObject);
            System.out.println("Pagination value in while loop : "+pagination);

            buildMap(responseObject, usersInteractionMap);
            nextUrl = pagination == true ? ((JSONObject) responseObject.get("responseMsg")).getString("@odata.nextLink")  : "";

        }

        List<Person> personList = new ArrayList<>();
        for (String name: usersInteractionMap.keySet()) {
            Person p = new Person(
                    name,
                    (String) usersInteractionMap.get(name).get("email"),
                    (Integer) usersInteractionMap.get(name).get("interactionsTotal"),
                    (Integer) usersInteractionMap.get(name).getOrDefault("interactionsAccepted", 0),
                    (Double) usersInteractionMap.get(name).get("meetingDurationTotal"),
                    (Double) usersInteractionMap.get(name).getOrDefault("meetingDurationAccepted", 0.0)
                    );
            personList.add(p);
        }

        Collections.sort(personList, (p1, p2) -> (p2.getMeetingsTotal() - p1.getMeetingsTotal()));



        return personList;
    }

    private JSONObject makeHttpCalendarCall(String urlString, String accessToken) throws  Exception{
        System.out.println("11111111");
        //urlString = "https://graph.microsoft.com/v1.0/me/joinedTeams";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }
        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        System.out.println("response"+responseObject);
        return responseObject;
    }

    private Boolean checkPagination(JSONObject responseObject) throws JSONException {
        Boolean containsPagination =  ((JSONObject) responseObject.get("responseMsg")).has("@odata.nextLink");
        return containsPagination;

    }

    private void buildMap(JSONObject responseObject, HashMap<String, HashMap<String, Object>> usersInteractionMap) throws JSONException {
        JSONArray tsmresponse = (JSONArray) (((JSONObject) responseObject.get("responseMsg")).get("value"));
        System.out.println("tsmresponse: "+tsmresponse);

        for(int i=0; i<tsmresponse.length(); i++){
            JSONArray attendeesList = (JSONArray) tsmresponse.getJSONObject(i).get("attendees");
            System.out.println("attendeesList"+attendeesList);

            String meetingStartTime = ((JSONObject )tsmresponse.getJSONObject(i).get("start")).getString("dateTime");
            String meetingEndTime = ((JSONObject )tsmresponse.getJSONObject(i).get("end")).getString("dateTime");

            LocalDateTime start = LocalDateTime.parse(meetingStartTime);
            LocalDateTime end = LocalDateTime.parse(meetingEndTime);

            Duration duration = Duration.between(end, start);
            double meetingDuration = Math.abs(duration.toMinutes());



            System.out.println("meetingStartTime: "+meetingStartTime);
            System.out.println("meetingEndTime: "+meetingEndTime);
            System.out.println("meetingDuration: "+meetingDuration);




            for(int j=0; j<attendeesList.length(); j++){
                String name = ((JSONObject) attendeesList.getJSONObject(j).get("emailAddress")).getString("name");
                boolean accepted = false;
                String responseString =  ((JSONObject) attendeesList.getJSONObject(j).get("status")).getString("response");
                System.out.println("responseString: "+responseString);
                if (responseString.equals("accepted")){
                    accepted = true;
                }
                System.out.println("name is "+name);
                if (usersInteractionMap.containsKey(name)){
                    usersInteractionMap.get(name).put("interactionsTotal", (Integer) usersInteractionMap.get(name).get("interactionsTotal")+1);
                    usersInteractionMap.get(name).put("meetingDurationTotal", (Double) usersInteractionMap.get(name).get("meetingDurationTotal") + meetingDuration);

                    if (accepted) {
                        usersInteractionMap.get(name).put("interactionsAccepted", (Integer) usersInteractionMap.get(name).getOrDefault("interactionsAccepted", 0) + 1);
                        System.out.println("1111111");
                        usersInteractionMap.get(name).put("meetingDurationAccepted", (Double) usersInteractionMap.get(name).getOrDefault("meetingDurationAccepted", 0.0) + meetingDuration);
                        System.out.println("22222");

                    }

                }
                else {
                    HashMap<String, Object> hm = new HashMap<>();
                    String emailAddress = ((JSONObject) attendeesList.getJSONObject(j).get("emailAddress")).getString("address");
                    hm.put("email", emailAddress);
                    hm.put("interactionsTotal", 1);
                    hm.put("meetingDurationTotal", meetingDuration);

                    if (accepted) {
                        hm.put("interactionsAccepted", 1);
                        hm.put("meetingDurationAccepted", (double)hm.getOrDefault("meetingDurationAccepted", 0)+meetingDuration);

                    }
                    usersInteractionMap.put(name, hm);

                }
                //people.put(emailAddress, people.getOrDefault(emailAddress, 0)+1);
                System.out.println("usersInteractionMap"+usersInteractionMap.keySet());

            }
        }

    }

    private void makeCall(String nextPageURL, String accessToken) throws Exception {
        URL url = new URL(nextPageURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        System.out.println("Final response 2 is "+responseObject.toString());

    }

    private void setAccountInfo(ModelAndView model, HttpServletRequest httpRequest) throws ParseException {
        IAuthenticationResult auth = SessionManagementHelper.getAuthSessionObject(httpRequest);

        String tenantId = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getStringClaim("tid");

        model.addObject("tenantId", tenantId);
        model.addObject("account", SessionManagementHelper.getAuthSessionObject(httpRequest).account());
    }
}
