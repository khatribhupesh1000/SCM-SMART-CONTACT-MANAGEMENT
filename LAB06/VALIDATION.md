# Assignment 6 validation checklist

Run with Java 21 and Maven installed:

```bash
mvn clean test
mvn spring-boot:run
```

Start the existing SCM Contact Service on port 8082 first. For deterministic rate-limit evidence, start this same service with `RATE_LIMIT_PER_MINUTE=2`. Capture real HTTP output:

```bash
CONTACT_REF='your-real-contact-id' ./capture-curl-transcript.sh
```

The generated `transcript-actual.txt` is the evidence file. `transcript.txt` is only the checklist/template. The preparation environment did not have Maven installed, so the Maven tests still need to be run locally.
