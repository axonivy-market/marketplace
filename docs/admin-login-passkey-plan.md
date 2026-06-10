# Admin Login V2 With GitHub OAuth + Passkey

## Summary

- Bottom line: add a new admin auth v2 flow, keep current token-based auth untouched, and avoid exposing GitHub tokens to FE.
- FE redirects to GitHub OAuth; GitHub redirects back to the FE callback route, which receives `code`.
- FE sends only `{ code, state }` to BE; FE never holds the GitHub access token.
- BE exchanges `code` for a GitHub access token internally (GitHub has no validate-from-code API), validates `axonivy-market` + `team-octopus` membership, then discards the token (not persisted, not returned), and creates a Spring Security session.
- OAuth credentials split: FE holds only `client_id` + callback URL (both public-safe); the GitHub OAuth App `client_secret` lives only on BE and is required for the `code` -> token exchange.
- Session stored in PostgreSQL via Spring Session JDBC for multinode BE.
- Passkey is an optional "stay signed in" convenience (Phase 2) so admins do not re-auth with GitHub every time; it uses WebAuthn, and 1Password save/autofill is triggered by browser credential APIs, not by BE returning a passkey.

## Key Changes

- Backend additive auth v2:
  - Add new `/auth/admin/v2/**` endpoints; do not change existing `/auth/github/**`, JWT service, or `@Authorized` behavior.
  - Add Spring Security `SecurityFilterChain` scoped to admin v2/passkey endpoints.
  - Add backend OAuth code exchange endpoint: `POST /auth/admin/v2/github/callback` with `{ code, state }`.
  - BE keeps the GitHub OAuth App `client_secret` server-side only (env/secret store); FE never receives it.
  - Validate OAuth `state` server-side to prevent CSRF/login CSRF.
  - Use existing GitHub membership check for `axonivy-market` and `team-octopus`.
  - Treat the exchanged GitHub access token as ephemeral: use it only for the membership validation call, then discard it. Do not persist it and do not return it to FE. (Encrypt-in-session only if a later feature actually needs it.)
  - Add `POST /auth/admin/v2/logout`, `GET /auth/admin/v2/session`.

- Backend session/security:
  - Add Spring Session JDBC with PostgreSQL tables for shared sessions.
  - Session cookie: `HttpOnly`, `Secure`, `SameSite=Lax` or `Strict` if UX allows, admin-specific name.
  - Add CSRF protection for cookie-authenticated mutating requests.
  - Add session fixation protection, max session lifetime, idle timeout, logout invalidation, audit logs, and rate limits for OAuth/passkey attempts.
  - IP binding deferred for v1 (risk of false logouts behind mobile/proxy networks). If added later: store the trusted-proxy-resolved client IP in session on login, add a filter that invalidates the session and returns `401` on mismatch, use only trusted reverse proxy headers, and never trust arbitrary `X-Forwarded-For` from public clients.

- Passkey (Phase 2 - optional "stay signed in" convenience, not required for admin v2 login):
  - Native WebAuthn needs Spring Security 6.4+ (Spring Boot 3.4). Since the repo is on Boot 3.2.5, prefer a dedicated WebAuthn library (e.g. Yubico `java-webauthn-server` / webauthn4j) to avoid bundling a framework upgrade with this feature; revisit a Boot upgrade separately.
  - Use JDBC/JPA-backed WebAuthn credential storage.
  - Register passkey only after a valid GitHub admin session already exists.
  - Authenticate passkey with username + WebAuthn assertion, then create the same Spring session - so admins skip the GitHub round-trip on return visits.
  - Require user verification when possible; use `residentKey=preferred` or `required` based on browser/1Password compatibility.

- UI additive v2:
  - Add new route like `/admin-login-v2`; keep `/request-access` unchanged.
  - Add new standalone components/services for admin v2 auth; do not rewrite current `AdminTokenComponent`.
  - UI buttons: `Login with GitHub`, `Login with passkey`.
  - GitHub callback route receives `code` and calls BE; no GitHub token stored in browser.
  - FE config holds only `client_id` + callback URL; never the `client_secret`.
  - After valid session, optionally show `Create passkey` (Phase 2).
  - Use `navigator.credentials.create()` and `navigator.credentials.get()` with BE options (Phase 2).
  - Admin v2 HTTP calls use `withCredentials`; no `sessionStorage` admin token.

## Minimal Current-Code Touches

- Avoid changing existing auth implementation.
- Required small integration edits:
  - Add new route entries.
  - Add new API constants/runtime config keys if needed.
  - Possibly add admin v2 guard for cookie session.
- Existing token flow remains available for fallback during rollout.

## Test Plan

- Backend:
  - OAuth code callback rejects missing/invalid state.
  - GitHub member creates session; non-member returns `401`.
  - GitHub access token never appears in response body, logs, or FE storage, and is not persisted after membership validation.
  - `client_secret` is read from BE config only and never exposed via any endpoint or response.
  - Session is readable across nodes through Spring Session JDBC.
  - CSRF required for mutating cookie-auth admin endpoints.
  - (If IP binding is later enabled) IP mismatch from trusted proxy invalidates session and spoofed public `X-Forwarded-For` is ignored.
  - (Phase 2) Passkey register/authenticate succeeds and rejects replay/invalid challenge.

- UI:
  - New login page shows GitHub login (and passkey option in Phase 2).
  - GitHub callback sends only `code/state`; no token handled in browser.
  - Requests include cookies via `withCredentials`.
  - (Phase 2) Passkey create uses `navigator.credentials.create`; passkey login uses `navigator.credentials.get`.
  - Existing `/request-access` tests still pass unchanged.

## Assumptions

- GitHub OAuth callback targets the FE route; FE forwards only `code` + `state` to BE.
- Direct GitHub OAuth returns `code`, so BE must exchange it; the GitHub access token is ephemeral and stays on BE.
- The GitHub OAuth App `client_secret` is provisioned in BE config/secret store; FE holds only `client_id` + callback URL.
- Bug bounty posture requires no frontend GitHub token exposure.
- Passkey is an optional convenience layer (Phase 2); admin v2 login works fully with GitHub OAuth alone.
- Current admin token flow remains untouched until a later migration task.
