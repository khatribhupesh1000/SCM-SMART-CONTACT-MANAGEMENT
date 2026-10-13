# Assignment 3 alignment correction — SCM Integration Service

The earlier Assignment 3 package used a fictional `QuickPing` SMS provider. That is not part of the uploaded SCM repository, so it should not be presented as the existing project integration.

The actual uploaded SCM project has an `integration-service` on port 8086. Its `IntegrationController` exposes:
- `GET /v1/soap/wsdl` — returns the `TelecomDirectoryService` WSDL.
- `GET /v1/soap/metadata` — returns parsed WSDL metadata.
- `POST /v1/soap/execute` — executes the SOAP bridge.
- `GET /v1/resilience/status` and `POST /v1/resilience/state` — resilience inspection/control.

The WSDL contract in `scm-telecom-directory.wsdl` is aligned with `SoapIntegrationService.TELECOM_WSDL_XML` in the uploaded project: service `TelecomDirectoryService`, operation `VerifyPhoneNumber`, SOAPAction `http://scm.enterprise.io/ws/telecom/VerifyPhoneNumber`, and the mock endpoint at `http://localhost:8086/v1/soap/mock-endpoint`.

The included request/response are illustrative samples for the contract, not a claim that a real external telecom partner is running. `soap-fault.xml` is a sample downstream SOAP fault used for the Assignment 4 error-mapping discussion; the current WSDL itself does not declare a named WSDL fault. Do not claim it does.
