/* taken from Open SAP course 'Next Steps In SAP HANA Cloud Platform' */

package com.sap.cloudsample;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OAuthClient {

    private static String client_id = "<COPY CLIENT ID FROM YOUR TRIAL ACCOUNTS' OAUTH CLIENT>";
    private static String callback_host = "http://localhost";
    private static int callback_port = 8000;
    private static String callback_path = "/oauthcallback";
    private static String scopes = "view-message create-message";
    private static String oauthas_authz_endpoint = "<COPY THE AUTHORIZATION ENDPOINT URL FROM YOUR TRIAL ACCOUNTS' OAUTH URLS >";
    private static String oauthas_token_endpoint = "<COPY THE TOKEN ENDPOINT URL FROM YOUR TRIAL ACCOUNTS' OAUTH URLS >";
    private static String api_endpoint = "<COPY APPLICATION URL FROM APPLICATION DASHBOARD OF YOUR APP FROM WEEK 5 UNIT 3>/api/v1/message";

    private static HttpServer server;

    private static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);

    private final static Logger log = Logger.getLogger(OAuthClient.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // start simple http server in a new thread to handle the OAuth AS
        // callback via HTTP redirect to localhost
        server = HttpServer.create(new InetSocketAddress(callback_port), 0);
        server.createContext(callback_path, new OAuthCallbackHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        log.setLevel(Level.INFO);
        try {
            // Steps 1 & 2: Request Authorization Code from OAuth 2.0 AS
            // Authorization Endpoint.
            // User will be redirected to the trusted IDP of your SAP HANA Cloud
            // Platform account, e.g. SAP ID Service.
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(oauthas_authz_endpoint + "?client_id=" + client_id + "&response_type=code&redirect_uri=" + callback_host + ":"
                    + callback_port + callback_path + "&scope=" + URLEncoder.encode(scopes, "UTF-8")));
            log.info("Browser opened and waiting for user's authorization...");
            // Step 3: Wait to obtain the Authorization Code from the Redirect
            // URI in Authorization Response
            String authorizationCode = queue.take();
            log.info("Received authorization code: " + authorizationCode);
            // Step 4: Use Authorization Code to request Access Token from OAuth
            // 2.0 AS Token Endpoint

            // IMPORTANT: Set to "false" in case you don't work behind a proxy
            // server
            OAuthHelper.setProxy(true);

            String accessToken = OAuthHelper.getAccessToken(authorizationCode);
            if (accessToken != null) {
                // Step 5 & 6: Invoke Web API with Access Token
                log.info("Invoke Web API to retrieve message");
                URL apiEndpoint = new URL(api_endpoint);
                HttpsURLConnection con = (HttpsURLConnection) apiEndpoint.openConnection();
                // add request header and access token
                con.setRequestMethod("GET");

                con.setRequestProperty("Authorization", "Bearer " + accessToken);

                int responseCode = con.getResponseCode();
                log.info("Response Code from Web API call: " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print Web API call result
                log.info(response.toString());
                con.disconnect();

                // Now let's retry this again
                log.info("Press return key to call API again with the same access token");
                System.in.read();
                con = (HttpsURLConnection) apiEndpoint.openConnection();
                // add request header and access token
                con.setRequestMethod("GET");

                con.setRequestProperty("Authorization", "Bearer " + accessToken);

                responseCode = con.getResponseCode();
                log.info("Response Code from Web API call: " + responseCode);

                if (responseCode != 200) {
                    con.disconnect();
                    log.info("Response message: " + con.getResponseMessage());
                } else {

                    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print Web API call result
                    log.info(response.toString());

                }
                con.disconnect();

            } else {
                log.info("Error requesting access token");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static class OAuthHelper {

        public static void setProxy(boolean on) {

            Properties systemSettings = System.getProperties();
            if (on) {
                systemSettings.put("https.proxyHost", "proxy");
                systemSettings.put("https.proxyPort", "8080");
            } else {
                systemSettings.put("https.proxyHost", "");
                systemSettings.put("https.proxyPort", "");
            }

        }

        public static String getAccessToken(String authorizationCode) throws IOException {
            URL tokenEndpoint = new URL(oauthas_token_endpoint);
            HttpsURLConnection con = (HttpsURLConnection) tokenEndpoint.openConnection();

            // add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters = "grant_type=authorization_code&code=" + authorizationCode + "&redirect_uri="
                    + URLEncoder.encode(callback_host + ":" + callback_port + callback_path, "UTF-8") + "&client_id=" + URLEncoder.encode(client_id, "UTF-8");

            // send POST request to Access Token endpoint
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            log.info("Sending Access Token request to URL : " + tokenEndpoint);
            log.info("POST parameters : " + urlParameters);
            log.info("Response Code : " + responseCode);
            log.info("Response Message: " + con.getResponseMessage());

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // log result
                log.info("Received Access Token Response: " + response.toString());

                // parse response for access token
                int beginIndex = response.toString().indexOf("access_token\":\"") + "access_token\":\"".length();
                int endIndex = response.toString().indexOf(",") - 1;

                String accessToken = response.toString().substring(beginIndex, endIndex);
                log.info("Access Token: " + accessToken);
                return accessToken;
            } else {
                return null;
            }
        }
    }

    static class OAuthCallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException, MalformedURLException {
            String authorizationCode = getAuthorizationCode(exchange.getRequestURI());
            String response = "Received authorization code from OAuth AS: " + authorizationCode;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.flush();
            os.close();
            // after receiving the authorization code, stop the HTTP server in
            // the separate thread
            server.stop(0);
            log.info("HTTP Server stopped");
            queue.add(authorizationCode);
        }

        private String getAuthorizationCode(URI redirectURI) throws MalformedURLException {
            log.info("Redirect URI: " + redirectURI.toString());
            String query = redirectURI.getQuery();
            String[] params = query.split("=");
            if (params[0].equals("code"))
                return params[1];
            else
                throw new MalformedURLException();
        }
    }
}
