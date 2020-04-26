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

