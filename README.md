# OAuth
Some notes about OAuth - mybe later some apps and tests
* https://github.com/SAP/cloud-security-xsuaa-integration
* https://blogs.sap.com/2020/04/03/sap-application-router/
* https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/01c5f9ba7d6847aaaf069d153b981b51.html
Info collected from various sources
## Roles
* resource owner (mostly the end-user) - capable of granting access to a protected resource 
* resource server (cloud application resp. micro service) - hosts protected resources (REST endpoints) - capable of accepting and responding to protected resource requests using access tokens - The API you want to access
* client (e.g. approuter see above) - application making protected resource requests on behalf of the resource owner and with its authorization
* authorization server (User Account and Authentication (UAA) service) 
  * o	issues access tokens for the client to obtain the authorizations of the resource owner after he was successfully authenticated by e.g. a SAML 2.0 compliant identity provider
## Grants
### Authorisation Code Grant ( https://tools.ietf.org/html/rfc6749#section-4.1 )
* sign into an app using e.g. your Google account
* client redirects user agent (e.g. browser) to the auth server's authorization endpoint (see authas_authz_endpoint below) with the following parameters:
  * *response_type*=code
  * *client_id*=" + client_id, OAuth Client identifier
  * *redirect_uri*=" + callback_host + ":" + callback_port + callback_path - optional, without user will be redirected to preregistered redirect URL
  * *scope* "&scope=" + URLEncoder.encode(scopes, "UTF-8") , private static String scopes = "view-message create-message";
  * *state* for CSRF
  
 ```
Desktop desktop = Desktop.getDesktop();
desktop.browse(new URI(oauthas_authz_endpoint + "?client_id=" + client_id + "&response_type=code&redirect_uri=" + callback_host + ":"
                    + callback_port + callback_path + "&scope=" + URLEncoder.encode(scopes, "UTF-8")));
 ```
* User approves the request => user agent will be redirected by the authoritzation server back to the client using *redirect_uri* with the params 
  * *code* the authorization code
  * *state* the CSRF state from above (2B checked)
  
* The client sends a POST request to the authorization servers token endpoint with the following parameters
  * *client_id*
  * *grant_type*="authorization_code"
  * *code*="authorizationCode"
  * *redirect_uri*=URLEncoder.encode(callback_host + ":" + callback_port + callback_path, "UTF-8")

```
String urlParameters = "grant_type=authorization_code&code=" + authorizationCode + "&redirect_uri="
          + URLEncoder.encode(callback_host + ":" + callback_port + callback_path, "UTF-8") 
          + "&client_id=" + URLEncoder.encode(client_id, "UTF-8");
```
* Authorization server issues an access token and optional refresh
   token (see https://tools.ietf.org/html/rfc6749#section-4.1.4)
 ```
      HTTP/1.1 200 OK
     Content-Type: application/json;charset=UTF-8
     Cache-Control: no-store
     Pragma: no-cache

     {
       "access_token":"2YotnFZFEjr1zCsicMWpAA",
       "token_type":"example",
       "expires_in":3600,
       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
       "example_parameter":"example_value"
     }

 ```
### Implicit Grant
    
  

