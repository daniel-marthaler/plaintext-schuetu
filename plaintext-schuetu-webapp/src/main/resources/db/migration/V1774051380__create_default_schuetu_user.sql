-- Default admin user for plaintext-schuetu
-- Password: admin (bcrypt encoded)
-- Only insert if table exists and user doesn't exist yet
INSERT INTO dbauthuser (id, username, password, email, enabled, mandat, deleted, created_date)
VALUES (
    nextval('dbauthuser_id_seq'),
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@plaintext.ch',
    true,
    'default',
    false,
    NOW()
);

-- Assign admin role
INSERT INTO dbauthuserrole (id, user_id, role)
SELECT nextval('dbauthuserrole_id_seq'), id, 'ROLE_ADMIN'
FROM dbauthuser WHERE username = 'admin';

INSERT INTO dbauthuserrole (id, user_id, role)
SELECT nextval('dbauthuserrole_id_seq'), id, 'ROLE_USER'
FROM dbauthuser WHERE username = 'admin';
