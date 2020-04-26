# OAuth
Some notes about OAuth - mybe later some apps and tests
* https://github.com/SAP/cloud-security-xsuaa-integration
* https://blogs.sap.com/2020/04/03/sap-application-router/
* https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/01c5f9ba7d6847aaaf069d153b981b51.html
Info collected from various sources
## Roles ( https://tools.ietf.org/html/rfc6749#section-1.1 )
* resource owner (mostly the end-user) - capable of granting access to a protected resource 
* resource server (cloud application resp. micro service) - hosts protected resources (REST endpoints) - capable of accepting and responding to protected resource requests using access tokens - The API you want to access
* client (e.g. approuter see above) - application making protected resource requests on behalf of the resource owner and with its authorization
* authorization server (User Account and Authentication (UAA) service) 
  * o	issues access tokens for the client to obtain the authorizations of the resource owner after he was successfully authenticated by e.g. a SAML 2.0 compliant identity provider
## Protocol Flow -> https://tools.ietf.org/html/rfc6749#section-1.2
```
     +--------+                               +---------------+
     |        |--(A)- Authorization Request ->|   Resource    |
     |        |                               |     Owner     |
     |        |<-(B)-- Authorization Grant ---|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(C)-- Authorization Grant -->| Authorization |
     | Client |                               |     Server    |
     |        |<-(D)----- Access Token -------|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(E)----- Access Token ------>|    Resource   |
     |        |                               |     Server    |
     |        |<-(F)--- Protected Resource ---|               |
     +--------+                               +---------------+
```
(B)-- Authorization Grant
* authorization code
* implicit
* resource owner password credentials
* client credentials

## Grants
### Authorisation Code Grant ( https://tools.ietf.org/html/rfc6749#section-4.1 )
* sign into an app using e.g. your Google account (user is the Resource Owner in this case)
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
* simplified flow for clients which are part of the user agent, i.e. the client is implemented in the browser e.g. within a single page web app
* the client is issued an access token directly (as result of the resource owner authorization)
* no intermediate authorization code is issued


### Resource Owner Password Credentials
* The resource owner password credentials (i.e., username and password) can be used directly as an authorization grant to obtain an access token. 



### Client Credentials
* requesting access to protected resources based on an authorization previously arranged with the authorization server.

    
## Client Registration ( https://tools.ietf.org/html/rfc6749#section-2 )  

