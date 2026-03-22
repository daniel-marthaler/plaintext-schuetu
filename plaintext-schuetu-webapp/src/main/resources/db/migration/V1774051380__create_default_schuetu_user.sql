-- Default admin user for plaintext-schuetu
-- Uses my_user_entity table (Spring Security user store)
INSERT INTO my_user_entity (id, username, password, roles)
SELECT nextval('my_user_entity_id_seq'), 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN,ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM my_user_entity WHERE username = 'admin');
