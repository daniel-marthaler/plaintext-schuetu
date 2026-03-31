-- Spiel: mannschaftA/B and schiri are ManyToOne (multiple Spiele per Mannschaft/Schiri)
ALTER TABLE spiel DROP CONSTRAINT IF EXISTS ukm5k0cumc1n75c4wwkbypr1pin;
ALTER TABLE spiel DROP CONSTRAINT IF EXISTS uk673c6lg1kopr23e4bncs7dbv;
ALTER TABLE spiel DROP CONSTRAINT IF EXISTS uk4ja8vhln1p9o60h64ob5yi0wd;

-- Gruppe: kategorie_id is ManyToOne (gruppeA + gruppeB per Kategorie)
ALTER TABLE gruppe DROP CONSTRAINT IF EXISTS ukke2ywveqgb3kqlpnebtmlauqp;

-- Penalty: gruppe_id is ManyToOne (penaltyA + penaltyB per Gruppe)
ALTER TABLE penalty DROP CONSTRAINT IF EXISTS ukpm8ve2l8fwgttccejydr2v5lj;

-- Kategorie: these are fine as unique (1:1) but we keep them
-- grosser_final_id, grosserfinal2_id, kleine_final_id, gruppea_id, gruppeb_id, penaltya_id, penaltyb_id
