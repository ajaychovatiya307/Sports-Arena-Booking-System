-- =====================================================================
-- Sports Arena Booking System — Master Seed Script
-- =====================================================================
-- Runs all four microservice seed scripts against MySQL in one shot.
-- Usage (PowerShell / cmd, from repo root):
--   mysql -u root -p < backend\seed-all.sql
-- =====================================================================

SOURCE backend/auth-service/src/main/resources/tablescript.sql;
SOURCE backend/user-service/src/main/resources/tablescript.sql;
SOURCE backend/catalog-service/src/main/resources/tablescript.sql;
SOURCE backend/booking-service/src/main/resources/tablescript.sql;

-- Quick verification counts
SELECT 'auth.user_credentials' AS name, COUNT(*) AS rows_ FROM auth_db.user_credentials
UNION ALL SELECT 'user.customer_profile', COUNT(*) FROM user_db.customer_profile
UNION ALL SELECT 'user.team',             COUNT(*) FROM user_db.team
UNION ALL SELECT 'catalog.arena_category',COUNT(*) FROM catalog_db.arena_category
UNION ALL SELECT 'catalog.arena_package', COUNT(*) FROM catalog_db.arena_package
UNION ALL SELECT 'booking.booking',       COUNT(*) FROM booking_db.booking;
