# Deploying Sports Arena for free

| Part | Platform | Free tier |
|---|---|---|
| Frontend (React) | **Vercel** (Hobby) | Static hosting, HTTPS, deploy on every push |
| 5 backend services | **Render** (Free web services, Docker) | 512 MB each, sleep after 15 min idle, 750 instance-hours/month shared |
| Database | **TiDB Cloud Starter** | MySQL-compatible, 5 GiB storage, TLS required |

Total time: about 45 minutes, most of it waiting for the first Render builds.

You need the code in a GitHub repository (Render and Vercel deploy from GitHub). Check that `git status` does not list anything you don't want public before pushing.

---

## 1. Database: TiDB Cloud Starter

1. Sign up at [tidbcloud.com](https://tidbcloud.com) and create a **Starter** cluster in **AWS Singapore (ap-southeast-1)**, the same region as the Render services.
2. Click **Connect**. Note the **host** (`gateway01.ap-southeast-1.prod.aws.tidbcloud.com`-style), **port** `4000`, **user** (looks like `abc123xyz.root`), and generate a **password**.
3. Load the demo data, including the admin account, from the **repository root** (the seed script uses relative paths):

   ```bash
   mysql -h <HOST> -P 4000 -u '<USER>' -p \
     --ssl-mode=VERIFY_IDENTITY --ssl-ca=/etc/ssl/cert.pem \
     < backend/seed-all.sql
   ```

   This creates `auth_db`, `user_db`, `catalog_db` and `booking_db` with sample data. On Linux the CA bundle is usually `/etc/ssl/certs/ca-certificates.crt`.

## 2. Backend: Render Blueprint

1. Sign up at [render.com](https://render.com) with GitHub.
2. **New → Blueprint**, pick the repository. Render reads [render.yaml](render.yaml) and lists five services.
3. Fill in the values it asks for:

   | Variable | Value |
   |---|---|
   | `DB_URL` (auth) | `jdbc:mysql://<HOST>:4000/auth_db?sslMode=VERIFY_IDENTITY&createDatabaseIfNotExist=true` |
   | `DB_URL` (user) | same, with `user_db` |
   | `DB_URL` (catalog) | same, with `catalog_db` |
   | `DB_URL` (booking) | same, with `booking_db` |
   | `DB_USERNAME` / `DB_PASSWORD` | the TiDB user and password (all four services) |
   | `AUTH_SERVICE_URL` | `https://sports-arena-auth.onrender.com` |
   | `USER_SERVICE_URL` | `https://sports-arena-user.onrender.com` |
   | `CATALOG_SERVICE_URL` | `https://sports-arena-catalog.onrender.com` |
   | `BOOKING_SERVICE_URL` | `https://sports-arena-booking.onrender.com` |
   | `CORS_ALLOWED_ORIGINS` (gateway) | `https://*.vercel.app` for now; tightened in step 4 |

4. Click **Apply**. The first Docker builds take several minutes each.
5. **Check the real URLs.** Render uses `https://<service-name>.onrender.com` unless the name is taken, in which case it adds a suffix. Open each service in the dashboard; if a URL differs from the table above, fix the matching `*_SERVICE_URL` in **Environment** on every service that uses it (Render restarts the service).
6. Open `https://<each-service>.onrender.com/actuator/health`. All five should return `{"status":"UP"}`.

## 3. Frontend: Vercel

1. Sign up at [vercel.com](https://vercel.com) with GitHub, then **Add New → Project** and import the repository.
2. Set **Root Directory** to `frontend`. Vercel detects Vite; [frontend/vercel.json](frontend/vercel.json) supplies the build command, output folder and page-refresh rewrites.
3. Under **Environment Variables** add:

   ```
   VITE_API_BASE_URL = https://sports-arena-gateway.onrender.com
   ```

   It is baked in at build time, so changing it later requires a redeploy.
4. Click **Deploy** and note the address, e.g. `https://sports-arena.vercel.app`.

## 4. Lock down CORS

In Render → `sports-arena-gateway` → **Environment**, change `CORS_ALLOWED_ORIGINS` to the exact Vercel address (comma-separate several if needed):

```
https://sports-arena.vercel.app
```

## 5. Verify

- [ ] The Vercel site loads, and refreshing `/dashboard` doesn't 404.
- [ ] Log in with the seeded admin account (credentials in `backend/auth-service/src/main/resources/tablescript.sql`); the dashboard shows customer, category and booking counts.
- [ ] Register a new customer, create a team and book a slot.
- [ ] The browser console shows no CORS or mixed-content errors.

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| First request takes ~1 minute or returns 502 | Services were asleep. Wait and retry; every service the request touches has to wake. |
| Admin actions return 403 right after waking | The gateway asked `auth` for the role before it was up. Refresh once it is awake. |
| Browser shows a CORS error | `CORS_ALLOWED_ORIGINS` doesn't match the exact site address (scheme included, no trailing slash). |
| Service logs show `Access denied` or SSL errors | Check the `<prefix>.root` username, the password, and `sslMode=VERIFY_IDENTITY` in `DB_URL`. |
| Calls between services fail | A `*_SERVICE_URL` doesn't match the service's real Render URL (step 2.5). |
| All services suspended near month end | The 750 free hours are shared by all five services. Avoid uptime pingers that keep every service awake. |

## Before sharing the link

This is a demo: passwords are stored in plain text and identity is passed in request headers (see "Design Notes and Limitations" in the README). Use only the seeded demo accounts, and keep real credentials out of the repository; all secrets above live in the Render and Vercel dashboards.
