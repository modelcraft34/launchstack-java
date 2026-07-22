**LaunchStack Java PRD**

*Spring Boot + Angular SaaS/Admin Starter Kit*

Version 0.2 \| Baslangic icin netlestirilmis satisa hazir core plan

# 1. Executive Summary

LaunchStack Java, Spring Boot ve Angular ile SaaS, admin panel, dashboard veya kurumsal web uygulamasi baslatmak isteyen developerlar icin hazir bir full-stack starter kit urunudur. Hedef, tekrar tekrar yazilan auth, user management, admin panel, Docker, Swagger ve dokumantasyon islerini hazir vererek kullanicinin kendi business modulune daha hizli gecmesini saglamaktir.

Ana deger onerisi: Stop rebuilding authentication, user management, and admin panels from scratch. Start your Spring Boot SaaS faster.

# 2. Product Positioning

| **Alan**         | **Net karar**                                                                                                             |
|------------------|---------------------------------------------------------------------------------------------------------------------------|
| Urun tipi        | Paid developer starter kit / source code product                                                                          |
| Ana paket        | Full Stack Core: Spring Boot backend + Angular admin panel                                                                |
| Baslangic hedefi | Satilabilir v1.0 core urun, basit demo degil                                                                              |
| Kullanici vaadi  | Login/register/user management/admin panel hazir; kendi domain modulunu eklemeye basla                                    |
| Fark             | Java/Spring Boot odakli, production-aware, dokumantasyonu guclu, client project kullanimina lisansla izin veren paketleme |

# 3. Target Customers

- Java backend developer: Spring Boot ile yeni urun baslatmak isteyen kisi.

- Full-stack developer/freelancer: Musteri projelerinde hizli admin panel ve backend altyapisi kurmak isteyen kisi.

- Indie hacker / micro SaaS kurucusu: Auth ve admin panel yerine business modele odaklanmak isteyen kisi.

- Junior-mid developer: Temiz katmanli Spring Boot + Angular mimarisi gormek isteyen kisi.

- Kucuk ekip: Yeni internal tool veya SaaS prototipine hizli baslamak isteyen ekip.

# 4. Problem

Yeni bir SaaS veya admin panel projesinde developerlarin ilk haftalari genellikle ayni temel islerle gecer: auth akisi, refresh token, kullanici/rol yonetimi, validation, exception handling, Swagger, Docker, frontend guard/interceptor ve dashboard iskeleti. Bunlar kritik ama her projede tekrar yazilan, business deger uretmeden zaman alan parcalardir.

# 5. Solution

LaunchStack Java, bu temel altyapiyi calisir halde verir. Kullanicinin hedefi paketi indirip calistirmak, admin olarak login olmak ve kendi Product, Order, Customer, Project veya Invoice gibi domain modullerini eklemeye baslamaktir.

# 6. User Journey

1.  Kullanici urunu Gumroad/Lemon Squeezy veya private GitHub repo uzerinden satin alir.

2.  Zip dosyasini indirir veya repo access alir.

3.  .env.example dosyasini .env olarak kopyalar ve database/email ayarlarini girer.

4.  docker compose up komutunu calistirir.

5.  Backend localhost:8080, frontend localhost:4200, Swagger localhost:8080/swagger-ui uzerinden acilir.

6.  Seed admin kullanici ile login olur.

7.  User management ve dashboard ekranlarini gorur.

8.  Docs icindeki "How to add a new module" rehberine gore kendi business entity/modulunu ekler.

# 7. v1.0 Core Scope

v1.0 kapsami, ilk satilabilir full-stack core urundur. Hedef dar bir demo degil, indirildiginde gercek projeye baslangic hissi veren calisir starter kit ortaya cikarmaktir.

## 7.1 Backend v1.0

- Java 17 ve Spring Boot 3.

- PostgreSQL ve JPA/Hibernate.

- Flyway migration.

- Spring Security tabanli JWT authentication.

- Refresh token akisi.

- Register, login, logout, refresh token endpointleri.

- Email verification akisi.

- Forgot password / reset password akisi.

- Role based authorization: ADMIN, USER gibi baslangic rolleri.

- User management API: list, detail, create, update, activate/deactivate.

- Global exception handling ve standart hata response modeli.

- Request validation ve anlamli validation mesajlari.

- Swagger/OpenAPI dokumantasyonu.

- Basic audit fields: createdAt, updatedAt, createdBy, updatedBy.

- Seed admin user.

- Dockerfile ve docker-compose ile calisir setup.

- Unit/integration test ornekleri: auth, user service, controller.

## 7.2 Frontend v1.0

- Angular admin panel.

- Login, register, forgot password, reset password ekranlari.

- Dashboard layout, sidebar, navbar.

- User list, user detail, create/edit user ekranlari.

- Auth guard.

- HTTP interceptor.

- Refresh token handling.

- Logout akisi.

- Basic responsive admin UI.

- Error/success notification sistemi.

- Environment config yapisi.

## 7.3 Documentation v1.0

- README: quick start, requirements, commands.

- Getting Started guide.

- Authentication flow guide.

- Authorization / role guide.

- How to add a new module/entity guide.

- Environment variables guide.

- Deployment guide: basic VPS/Docker deployment.

- Customization guide.

- License guide.

# 8. Out of Scope for v1.0

- Multi-tenant architecture.

- Subscription billing / payment integration.

- Complex permission matrix / policy engine.

- Notification center.

- File upload module.

- Kubernetes deployment templates.

- Microservice architecture.

- AI features.

- Advanced analytics.

Bu ozellikler degerlidir fakat v1.0 urunu sisirir. v1.0 hedefi calisir full-stack core altyapidir; ileri ozellikler v1.5/v2.0 roadmapinde yer alir.

# 9. Packaging and Licensing Strategy

Tek kod tabani gelistirilir: LaunchStack Java Full Stack Core. Paketler, ayni core urunun farkli kullanim ve lisans seviyeleridir.

| **Paket**  | **Icerik**                                                                           | **Kullanim**                                           | **Fiyat fikri** |
|------------|--------------------------------------------------------------------------------------|--------------------------------------------------------|-----------------|
| Basic      | Backend source code, Docker, README, basic docs                                      | Kisisel proje / ogrenme / backend-only baslangic       | \$29-\$49       |
| Full Stack | Backend + Angular admin + full docs + Docker setup                                   | Kendi SaaS/admin projesi icin ana paket                | \$79-\$149      |
| Pro        | Full Stack + commercial license + private repo access + future updates + setup video | Freelancer/client project kullanimi ve premium deneyim | \$149-\$299     |

## 9.1 License Types

| **Lisans**         | **Izin verilen**                                                    | **Yasaklanan**                                                                                        |
|--------------------|---------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| Personal License   | Kendi projende ve ogrenme amacli kullanabilirsin.                   | Starter kit kaynak kodunu yeniden satamaz, paylasamaz veya musteriye standalone olarak devredemezsin. |
| Commercial License | Musteri projelerinde ve ticari urunlerde kullanabilirsin.           | Starter kitin kendisini veya cok az degistirilmis halini yeniden satamazsin.                          |
| Team License       | Kucuk ekip icinde kullanabilir, repo access ve updates alabilirsin. | Kodun ekip disina dagitimi veya marketplace olarak yeniden satimi yasaktir.                           |

# 10. Repository Structure

Tek repo / monorepo yapisi tercih edilir:

launchstack-java  
\|-- backend  
\| \|-- src/main/java  
\| \|-- src/main/resources  
\| \|-- src/test/java  
\| \|-- Dockerfile  
\| \`-- pom.xml  
\|-- frontend  
\| \|-- src/app  
\| \|-- src/assets  
\| \|-- Dockerfile  
\| \`-- package.json  
\|-- docs  
\| \|-- getting-started.md  
\| \|-- authentication.md  
\| \|-- authorization.md  
\| \|-- deployment.md  
\| \|-- customization.md  
\| \`-- how-to-add-module.md  
\|-- docker-compose.yml  
\|-- .env.example  
\|-- README.md  
\`-- LICENSE

# 11. Backend Module Structure

com.launchstack  
\|-- auth  
\| \|-- controller  
\| \|-- service  
\| \|-- dto  
\| \|-- token  
\| \`-- security  
\|-- user  
\| \|-- controller  
\| \|-- service  
\| \|-- dto  
\| \|-- mapper  
\| \|-- entity  
\| \`-- repository  
\|-- role  
\|-- email  
\|-- audit  
\|-- common  
\| \|-- exception  
\| \|-- response  
\| \|-- validation  
\| \`-- config  
\`-- config

# 12. Frontend Module Structure

src/app  
\|-- core  
\| \|-- guards  
\| \|-- interceptors  
\| \|-- services  
\| \`-- models  
\|-- shared  
\|-- layout  
\| \|-- sidebar  
\| \|-- navbar  
\| \`-- dashboard-layout  
\|-- features  
\| \|-- auth  
\| \|-- dashboard  
\| \`-- users  
\`-- app.routes.ts

# 13. API Scope v1.0

| **Alan** | **Endpointler**                                                                                                                                              |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Auth     | POST /auth/register, POST /auth/login, POST /auth/refresh, POST /auth/logout, POST /auth/verify-email, POST /auth/forgot-password, POST /auth/reset-password |
| Users    | GET /users, GET /users/{id}, POST /users, PUT /users/{id}, PATCH /users/{id}/status                                                                          |
| Roles    | GET /roles                                                                                                                                                   |
| Profile  | GET /me, PUT /me                                                                                                                                             |

# 14. Roadmap

| **Versiyon**      | **Hedef**                                           | **Kapsam**                                                                    |
|-------------------|-----------------------------------------------------|-------------------------------------------------------------------------------|
| v0.1 Internal MVP | Temel akisin calistigini kanitla                    | Backend auth + Angular login + Docker local setup                             |
| v0.5 Beta         | Disaridan bir developerin kurup deneyebilecegi beta | User management, refresh token, docs, Swagger, daha temiz UI                  |
| v1.0 Paid Launch  | Satilabilir ilk full-stack core                     | Full Stack core, demo, screenshots, license, packaging, Gumroad/Lemon Squeezy |
| v1.5 Pro Features | Degeri ve fiyatlamayi artir                         | Payment, file upload, notification center, audit UI, daha fazla test          |
| v2.0 Advanced     | Daha buyuk ekip ve SaaS senaryolari                 | Multi-tenant, team/workspace, deployment templates, advanced permissions      |

# 15. v1.0 Acceptance Criteria

- Yeni bir kullanici README ile projeyi 30 dakika icinde localde calistirabilmeli.

- docker compose up ile backend, frontend ve PostgreSQL ayaga kalkmali.

- Seed admin ile login olunabilmeli.

- Register/email verification/forgot password akislari calisir olmali veya local email simulator ile test edilebilmeli.

- Swagger endpointleri gosteriyor olmali.

- Angular auth guard ve refresh token handling calismali.

- User list/detail/create/edit ekranlari calisir olmali.

- How to add a new module rehberiyle ornek bir domain modulu eklenebilmeli.

- Kod temiz, katmanli ve customization icin anlasilir olmali.

# 16. 30-Day Build Plan

| **Hafta** | **Cikti**                                                                                |
|-----------|------------------------------------------------------------------------------------------|
| Hafta 1   | PRD v0.2, Prompt Playbook v0.2, repo setup, backend skeleton, database/Flyway kararlari  |
| Hafta 2   | Backend auth, JWT, refresh token, user/role, exception handling, Swagger, Docker         |
| Hafta 3   | Angular auth flow, dashboard layout, user management UI, interceptor/guard               |
| Hafta 4   | Docs, demo, screenshots, license, packaging, landing page taslagi, beta tester hazirligi |

# 17. Launch Assets

- Landing page headline: Stop rebuilding authentication, user management, and admin panels from scratch.

- Demo video: 2-3 dakikada local setup, login, user management ve yeni module ekleme ozeti.

- Screenshots: login, dashboard, user list, Swagger, code structure.

- README ve docs preview.

- Pricing table: Basic, Full Stack, Pro.

- License summary: Personal, Commercial, Team.

# 18. Immediate Next Steps

9.  Bu PRD v0.2 dosyasini baslangic urun karari olarak kabul et.

10. Prompt Playbook v0.2 ile coding agent/Claude tarafinda repo setup sprintini baslat.

11. Ilk hedef: v0.1 Internal MVP. Backend auth + Angular login + Docker local setup.

12. Her sprint sonunda PRD ve playbook degisikliklerini versiyonla.
