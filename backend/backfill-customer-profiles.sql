-- ============================================================================
-- One-time backfill: create customer_profile rows for any CUSTOMER user in
-- auth_db.user_credentials that is missing a profile in user_db.customer_profile.
-- Fixes US2-b (e.g. U1/U2 present in auth but missing in the Customers list).
--
-- Safe to re-run: the NOT EXISTS guard + unique(user_id) prevent duplicates.
-- Run as a user that can read auth_db and write user_db.
-- ============================================================================

INSERT INTO user_db.customer_profile (user_id, full_name, email, contact_number, address)
SELECT uc.user_id,
       uc.full_name,
       uc.email,
       NULL,
       NULL
FROM auth_db.user_credentials uc
WHERE uc.role = 'CUSTOMER'
  AND NOT EXISTS (
        SELECT 1 FROM user_db.customer_profile cp
        WHERE cp.user_id = uc.user_id
      );

-- Verify
SELECT cp.user_id, cp.full_name, cp.email
FROM user_db.customer_profile cp
ORDER BY cp.user_id;
