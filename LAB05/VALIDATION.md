# Assignment 5 validation checklist

Run from this folder with Java 21 and Maven installed:

```bash
mvn clean test
mvn spring-boot:run
```

In another terminal, validate the contract (if `openapi-spec-validator` is installed):

```bash
openapi-spec-validator openapi.yaml
```

The OpenAPI YAML is suitable for https://editor.swagger.io/. Do not submit a claim that tests or external validation passed unless you ran them and saved the output. `curl-transcript.txt` is an evidence template; replace it with actual `curl -i`/`curl -v` output from your running SCM Contact Service and this service.
