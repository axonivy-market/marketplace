---
applyTo: "**"
---
When reviewing code, focus on:

## General Standards
- New CMS entries should be capitalized and placed in `cms/` folder
- Avoid inline-style in xhtml or html files, use css instead
- Use `===` not `==` in Javascript files
- Avoid warnings in code, if you cannot avoid it, add a comment explaining why

### Security Standards
- Authentication bypass
- Authorization flaws
- Privilege escalation
- Session fixation
- CSRF
- Open Redirect
- SSRF
- Path Traversal
- XSS
- XXE
- XML Bomb
- Insecure Deserialization
- Zip Slip
- Regex DoS
- Resource exhaustion
- Timing attacks
- Information disclosure
- Hardcoded secrets
- Weak cryptography
- Missing security headers
- Unsafe file upload
- Unsafe download
- Unsafe redirect
- Unsafe reflection
- Unsafe ProcessBuilder usage
- Unsafe Runtime.exec usage
- Unsafe ObjectInputStream usage
- Unsafe Jackson polymorphic deserialization

#### Reported Security Issues
1. API Key and Secret Found on GitHub – Potential for Unauthorized Access
2. NPM dependencies that are in private repositories can be overwritten in the public repo if there is no scope.
3. Exposed GitHub OAuth Client Secret in Public GitHub Repository
4. Cleartext HTTP Traffic Enabled with android:usesCleartextTraffic="true" -> Potential for Man-in-the-Middle Attacks
5. Application Data Backup Enabled via ADB with android:allowBackup="true" -> Potential for Data Leakage
6. Insecure Randomness Usage with java.util.Random -> Potential for Predictable Values
7. LDAP Injection allows authentication bypass and directory enumeration via unsanitized filter input in LdapQueryExecutor.java - unsantized filter input -> Potential for Authentication Bypass and Directory Enumeration
8. Path traversal (arbitrary file read/write) and disabled host key verification (MITM) in SftpClientService.java enabling RCE - No path traversal validation & man in the middle -> Potential for Arbitrary File Read/Write and Remote Code Execution
9. JavaScript Injection (XSS) - No escaping or sanitization is applied between decode and concatenation
  e.g.
  ```
  String statement = "parent.redirectToUrlCommand([{name: 'url', value: '" + URLDecoder.decode(url, StandardCharsets.UTF_8) + "'}])";
  PrimeFaces.current().executeScript(statement);
  ```
10. There is no Authorization or X-Authorization header on this request.
11. Missing @Authorized for Rest API endpoints, allowing unauthorized access to sensitive data and operations.
12. Unauthenticated file upload on https://market.axonivy.com. The Axon Ivy Market platform (market.axonivy.com) allows users to upload files (SVG, JSON, MD, XML). These files are then publicly accessible via the official domain — and remain so until they are overwritten by a new upload. -> The core issue is the "trusted domain" principle: people and security systems trust URLs that come from well-known, reputable domains. e.g. https://market.axonivy.com/uploads/malicious-file.svg. An attacker can exploit this in several ways: Phishing, Malware Distribution, Social Engineering, SVG-based Attacks.
13. The login page accepts a ?originalUrl= parameter that is mapped to out.callbackUrl. After a successful login, redirect without validating the target URL. This could allow an attacker to craft a legitimate-looking link that redirects to an external domain post-login, enabling phishing or credential laundering.
14. Reflected XSS on a core, authenticated Portal page, with no CSP in front of it.
The sanitizer (SanitizeAPI.escapeForJavascript) is a shared helper used by many places. Right now it correctly escapes $ and { so the attack can't get through.
  ```
  <script>
    $(document).ready(function() {
    var taskUrl = `#{htmlSanitizerBean.escapeForJS(iFrameTaskTemplateBean.taskUrl)}`;
    ...
  ```
15. Missing check valid source for src of iframe. The iframe src is set to a user-controlled value without any validation. This could allow an attacker to craft a legitimate-looking link that redirects to an external domain post-login, enabling phishing or credential laundering. -> Sanitization of the iframe src is needed to ensure that only trusted sources are allowed.
16. The Search was vulnerable to Stored XSS. A malicious payload injected into a relevant field was reflected unsanitized in the search results, allowing script execution in the victim's browser.
17. Arbitrary URL navigation including javascript: URIs. Phishing via redirect to attacker sites from the trusted Portal domain. Combined with F1 this creates a prompt-injection → code-execution chain.
18. Per the public javadoc, only the numeric overload find(long id) requires Read/Read@SYSTEM; the String-UUID overload has no such permission requirement and acts as an opaque-capability lookup. Combined with the missing JSF gate, any authenticated user with knowledge of a UUID receives the full case payload — including description, custom fields, documents, workflow event audit trail, case hierarchy, linked task UUIDs, and the process-viewer iframe URL (which itself reveals the process-model version and highlighted element).
19. The scanner does not fully account for SVG SMIL elements that can dynamically assign dangerous attributes such as xlink:href or event handler attributes. It also does not canonicalize URL schemes before checking for dangerous protocols, so encoded TAB/LF characters inside javascript: URLs can bypass the scanner.
Solution:
a. Block all SMIL animation elements outright (animate, set, animateTransform, animateMotion, animateColor) — no legitimate Portal SVG needs them.
b. Match elements by local name — strip namespace prefix before lookup so svg:script, svg:animate, etc. are caught.
c. Reject <!ENTITY> declarations before parsing — prevents entity-based payload hiding.
d. Canonicalize URL schemes — strip both raw control characters (chars <= 0x20, 0x7F) and their percent-encoded forms (%09, %0A, etc.) before the dangerous-scheme regex check.
e. Add SVG security notes to logo and process-image documentation.