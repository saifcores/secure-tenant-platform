# ADR-002 — OAuth2/OIDC with Keycloak

## Status

Accepted

## Context

The API must not store passwords. Tokens must carry `tenant_id` and roles, and be validated for issuer, audience, expiry and signature.

## Decision

- Keycloak is the identity provider (realm `securetenant`).
- Spring Boot is an OAuth2 resource server (`spring-boot-starter-security-oauth2-resource-server`).
- Realm roles `PLATFORM_ADMIN`, `TENANT_ADMIN`, `MANAGER`, `USER`, `AUDITOR` are mapped to `ROLE_*`.
- `tenant_id` is a user attribute protocol mapper on the access token.
- Audience mapper emits `securetenant-api`.

## Consequences

- Password grant is enabled on a public demo client for scripts and tests only; production should use authorization code + PKCE.
- The API starts only when the issuer JWKS is reachable (except tests that provide a `JwtDecoder` bean).
