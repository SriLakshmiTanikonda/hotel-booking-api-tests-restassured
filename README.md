# Rest Assured API Testing Framework

[![API Tests](https://github.com/SriLakshmiTanikonda/hotel-booking-api-tests-restassured/actions/workflows/api-tests.yml/badge.svg)](https://github.com/SriLakshmiTanikonda/hotel-booking-api-tests-restassured/actions/workflows/api-tests.yml)

API test automation in Java with Rest Assured and TestNG, run against [Restful Booker](https://restful-booker.herokuapp.com), a public hotel-booking API made for testing practice. Test data is stored in a SQL database and pulled with queries.

## What it covers

| Test class | What it proves |
|---|---|
| `AuthTests` | Health check, token login, data-driven bad credentials |
| `BookingCrudTests` | Create, read, search, full update (PUT), partial update (PATCH), delete, JSON schema contract |
| `BookingNegativeTests` | Updates and deletes without a valid token are refused, unknown IDs return 404, bad request bodies are rejected |
| `DataDrivenBookingTests` | Bookings built from SQL query results; API response compared field by field with the database row; date math cross-checked against a SQL calculation |

Every response is also held to a response-time budget (5 seconds by default, configurable).

## Design

```
src/test/java/com/qa/api/
  config/    ApiConfig: base URI, credentials and limits from -D flags or environment variables
  specs/     Shared request spec (JSON headers) and response spec (time budget)
  clients/   BookingClient: one method per endpoint
  models/    Java records for request and response bodies (Jackson serialization)
  data/      TestDataDb (H2 in-memory SQL database) and BookingFactory (unique test data)
  tests/     TestNG test classes
src/test/resources/
  schemas/   JSON schemas for contract checks
  sql/       schema.sql and data.sql for the test-data database
  testng-regression.xml, testng-smoke.xml
```

Choices and why:

- **Client layer.** Tests read like behavior (`client.create(booking)`), and an endpoint change is fixed in one place.
- **Records as models.** Comparing whole objects (`assertThat(fetched).isEqualTo(booking)`) checks every field in one line, so no field gets skipped by accident.
- **Tests own their data.** Every test creates the booking it needs, with a unique name, and deletes it afterwards. Tests can run in parallel and in any order, even on a shared server.
- **Schema validation** catches breaking contract changes (a renamed field or a changed type) that value checks would miss.
- **SQL-driven data.** Input rows come from `SELECT` queries with filters, `COALESCE` and date arithmetic, which mirrors validating an API against its backing database.
- **Logging only on failure.** The full request and response are printed only for a failing check, so CI logs stay readable.
- **No secrets in the repo.** The demo credentials are defaults; Jenkins injects real ones from its credential store.

## Defects found in the API under test

This API has behavior that would be logged as a bug on a real project. The tests document it:

1. **Bad credentials return HTTP 200** with `{"reason": "Bad credentials"}` instead of 401.
2. **A successful DELETE returns 201 Created** instead of 200 or 204.
3. **Some invalid request bodies return 500** (server error) instead of 400 (client error).
4. **The health check returns 201** instead of 200.

The tests pin the current behavior, so a fix (or a new regression) shows up as a deliberate test change.

## Run it locally

Requires Java 17 or later and Maven 3.9 or later.

```bash
mvn test                       # full regression suite
mvn test -Dsuite=smoke         # smoke tests only
mvn test -Dapi.base.uri=https://your-env.example.com -Dapi.username=... -Dapi.password=...
```

TestNG's HTML report is written to `target/surefire-reports/index.html`.

## CI/CD

- **GitHub Actions** runs the suite on every push and pull request plus nightly, uploads the report, and posts a test summary. A manual run can pick the smoke or regression suite.
- **Jenkins** uses the `Jenkinsfile`, with parameters for suite and environment, and takes credentials from the Jenkins credential store.

## Tech stack

Java 17 · Rest Assured 5 · TestNG · AssertJ · Hamcrest · JSON Schema Validator · Jackson · H2 / SQL · Maven · GitHub Actions · Jenkins
