**LaunchStack Java Prompt Playbook**

*Claude / Coding Agent icin kontrollu gelistirme promptlari*

Version 0.2 \| Baslangic icin netlestirilmis satisa hazir core plan

# 1. Purpose

Bu dokuman, LaunchStack Java urununu coding agent/Claude/Codex benzeri araclarla kontrollu ve tutarli sekilde gelistirmek icin kullanilacak prompt setidir. Ana kural: agent tek seferde butun urunu yazmayacak; sprint sprint, test edilebilir parcalar uretecek.

# 2. Global Rules for Coding Agent

- PRD v0.2 kapsam disina cikma.

- v1.0 icin kararlastirilan core urunu hedefle; multi-tenant, payment, notification, file upload ekleme.

- Kod production-aware olsun ama gereksiz enterprise karmasikligi olmasin.

- Her adimda calistirma komutlari, test komutlari ve degisen dosyalari belirt.

- Once plan ver, sonra kodla. Belirsiz konu varsa varsayimlarini yaz.

- Secret/key/hardcoded password koyma; .env.example kullan.

- Kod okunabilir, moduler ve dokumante edilebilir olsun.

# 3. Master Context Prompt

Her yeni coding agent oturumunun basina eklenecek ana context:

You are the senior full-stack engineer building LaunchStack Java.  
  
LaunchStack Java is a paid Spring Boot + Angular SaaS/Admin Starter Kit.  
The goal is not a toy demo. The goal is a sellable v1.0 starter kit that helps Java developers stop rebuilding authentication, user management, and admin panels from scratch.  
  
Core stack:  
- Java 17  
- Spring Boot 3  
- Spring Security  
- PostgreSQL  
- Flyway  
- JWT authentication  
- Refresh tokens  
- Angular admin panel  
- Docker Compose  
- Swagger/OpenAPI  
  
v1.0 must include:  
- register/login/logout/refresh  
- email verification  
- forgot/reset password  
- role based authorization  
- user management APIs and UI  
- global exception handling  
- validation  
- seed admin user  
- Angular auth guard/interceptor  
- dashboard layout  
- strong README and docs  
  
Do NOT add in v1.0:  
- multi-tenancy  
- payment integration  
- subscription billing  
- notification center  
- file upload  
- Kubernetes  
- microservices  
- AI features  
  
Working style:  
- Work in small, verifiable steps.  
- Before coding, explain the plan and files you will create/change.  
- After coding, provide run commands and test commands.  
- Keep the code clean, layered, and easy for buyers to customize.

# 4. Sprint 0 - Repository Setup Prompt

Using the Master Context, create the initial monorepo structure for LaunchStack Java.  
  
Create:  
- /backend Spring Boot 3 Java 17 Maven project  
- /frontend Angular project placeholder structure  
- /docs folder  
- docker-compose.yml with PostgreSQL  
- .env.example  
- root README.md skeleton  
  
Backend should include only minimal bootstrapping in this step:  
- application.yml using env variables  
- health endpoint or simple actuator readiness if appropriate  
- package structure matching PRD v0.2  
  
Do not implement authentication yet.  
Output:  
1. file tree  
2. created files  
3. run commands  
4. next recommended sprint

# 5. Sprint 1 - Backend Foundation Prompt

Implement the backend foundation for LaunchStack Java.  
  
Scope:  
- PostgreSQL connection  
- Flyway migration setup  
- common API response model  
- global exception handling  
- validation error response  
- base audit fields createdAt/updatedAt  
- Swagger/OpenAPI config  
  
Do not implement auth yet.  
Create tests where useful.  
After implementation, provide run and test commands.

# 6. Sprint 2 - Auth Core Prompt

Implement authentication core for LaunchStack Java.  
  
Scope:  
- User entity  
- Role entity  
- UserRepository, RoleRepository  
- password hashing  
- register endpoint  
- login endpoint  
- JWT access token generation  
- refresh token entity/table  
- refresh token endpoint  
- logout endpoint that invalidates refresh token  
- seed admin user  
  
Rules:  
- Use DTOs for requests/responses.  
- Never expose password hashes.  
- Use clear exception types.  
- Add Flyway migrations.  
- Add unit/integration tests for critical flows.  
- Keep code easy to customize for buyers.

# 7. Sprint 3 - Email Verification and Password Reset Prompt

Add email verification and forgot/reset password flows.  
  
Scope:  
- email verification token table  
- send verification email service abstraction  
- local/dev email logging implementation  
- verify email endpoint  
- forgot password endpoint  
- reset password endpoint  
- token expiry and one-time usage  
  
Rules:  
- Do not require real SMTP for local setup; provide env-based SMTP config and dev logging fallback.  
- Document the flow in /docs/authentication.md.  
- Add tests for token expiry and invalid token.

# 8. Sprint 4 - Authorization and User Management API Prompt

Implement role based authorization and user management APIs.  
  
Scope:  
- ADMIN and USER roles  
- method/security config for protected endpoints  
- GET /users  
- GET /users/{id}  
- POST /users  
- PUT /users/{id}  
- PATCH /users/{id}/status  
- GET /roles  
- GET /me and PUT /me  
  
Rules:  
- Only ADMIN can manage users.  
- Authenticated users can read/update their profile where safe.  
- Use pagination for user list.  
- Add Swagger annotations or OpenAPI-friendly DTOs.  
- Add tests for authorization.

# 9. Sprint 5 - Angular Foundation Prompt

Build the Angular frontend foundation.  
  
Scope:  
- Angular project structure per PRD v0.2  
- core services  
- auth service  
- token storage service  
- HTTP interceptor  
- auth guard  
- app routes  
- dashboard layout with sidebar/navbar  
- basic styling, clean admin UI  
  
Do not build all feature pages yet.  
Provide commands to install and run.

# 10. Sprint 6 - Angular Auth Screens Prompt

Implement Angular auth screens.  
  
Scope:  
- login page  
- register page  
- forgot password page  
- reset password page  
- email verification result page if useful  
- logout flow  
- refresh token handling  
- error/success notification UI  
  
Rules:  
- Keep UI clean, product-ready, not toy-like.  
- Use environment API base URL.  
- Match backend DTOs.  
- Add guards so logged-in users do not see login page unnecessarily.

# 11. Sprint 7 - Angular User Management Prompt

Implement Angular user management screens.  
  
Scope:  
- user list with pagination/search placeholder  
- user detail  
- create/edit user form  
- activate/deactivate user action  
- role display and assignment if supported by backend  
  
Rules:  
- Only ADMIN route should access these screens.  
- Use reusable components where reasonable.  
- Keep forms validation aligned with backend validation.

# 12. Sprint 8 - Docker and Local Setup Prompt

Finalize Docker and local setup.  
  
Scope:  
- backend Dockerfile  
- frontend Dockerfile  
- docker-compose.yml for backend, frontend, PostgreSQL  
- .env.example complete  
- local dev instructions  
- seed admin user documented  
  
Acceptance:  
A buyer should be able to run the product locally using README instructions.

# 13. Sprint 9 - Documentation Prompt

Create buyer-facing documentation for LaunchStack Java.  
  
Create or update:  
- README.md  
- docs/getting-started.md  
- docs/authentication.md  
- docs/authorization.md  
- docs/how-to-add-module.md  
- docs/deployment.md  
- docs/customization.md  
- docs/license.md  
  
Tone:  
Clear, practical, developer-friendly. This is a paid starter kit, so docs must reduce buyer anxiety.  
Include commands, examples, and troubleshooting notes.

# 14. Sprint 10 - Packaging and Release Prompt

Prepare LaunchStack Java for v1.0 paid launch packaging.  
  
Scope:  
- clean file tree  
- remove unused code  
- confirm .env.example contains no secrets  
- add LICENSE placeholder files for package types  
- add CHANGELOG.md  
- add VERSION file or release notes  
- create packaging checklist  
- create demo script for a 2-3 minute walkthrough video  
  
Do not publish. Prepare the release candidate only.

# 15. PRD Update Prompt

Bu prompt, sprintlerden sonra PRD veya dokuman degisikligi gerekiyorsa kullanilacak:

Review the current LaunchStack Java implementation against PRD v0.2.  
  
Output:  
1. implemented features  
2. missing v1.0 features  
3. scope creep risks  
4. suggested PRD updates  
5. suggested Prompt Playbook updates  
  
Do not suggest v1.5/v2.0 features unless they are clearly out of v1.0 scope.

# 16. Quality Gate Checklist

- Proje localde README ile kurulabiliyor mu?

- docker compose up sorunsuz mu?

- Backend testleri geciyor mu?

- Angular build geciyor mu?

- Seed admin login calisiyor mu?

- Refresh token akisi calisiyor mu?

- Email verification ve reset password local/dev modda test edilebilir mi?

- Swagger aciliyor mu?

- Docs alici icin yeterince net mi?

- v1.0 out-of-scope ozellikler yanlislikla eklenmedi mi?

# 17. How to Use This Playbook

1.  Her coding agent oturumuna Master Context ile basla.

2.  Sadece ilgili sprint promptunu ekle.

3.  Agenttan once plan, sonra kod iste.

4.  Cikan kodu localde calistir ve hatalari yeni, kucuk promptlarla duzelt.

5.  Sprint tamamlaninca degisen karar varsa PRD/Playbook versiyonunu guncelle.
