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
### Authorisation Code Grant
  * sign into an app using e.g. your Google account
  * client redirects user to the auth server (see authas_authz_endpoint below) with the following parameters:
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
* User approves the request => client will be redirected back to *redirect_uri* with the params 
   * *code* the authorization code
   * *state* the CSRF state from above (2B checked)
     
  

