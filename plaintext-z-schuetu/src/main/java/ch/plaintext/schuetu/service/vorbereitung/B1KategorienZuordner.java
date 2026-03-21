package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.entity.Gruppe;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.comperators.MannschaftsNamenComperator;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import ch.plaintext.schuetu.repository.GruppeRepository;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class B1KategorienZuordner {

    @Autowired private MannschaftRepository mannschaftRepo;
    @Autowired private GruppeRepository gruppeRepo;
    @Autowired private KategorieRepository kategorieRepo;

    public void automatischeZuordnung(String game) {
        Map<String, Kategorie> map = zuordnungVornehmen(mannschaftRepo.findByGame(game), game);
        Set<String> keys = map.keySet();
        List<String> mKeys = new ArrayList<>(); List<String> kKeys = new ArrayList<>();
        for (String key : keys) { if (key.contains("M")) { mKeys.add(key); } else { kKeys.add(key); } }
        Collections.sort(kKeys); zuKleineGruppenInAndereKategorieSchieben(map, kKeys);
        Collections.sort(mKeys); zuKleineGruppenInAndereKategorieSchieben(map, mKeys);
        for (Kategorie kat : map.values()) {
            Kategorie kategorie = kategorieRepo.findById(kat.getId()).get();
            String hintS = null;
            List<Mannschaft> m = kategorie.getGruppeA().getMannschaften();
            for (Mannschaft mann : m) {
                if (mann.getSpielWunschHint() != null && !mann.getSpielWunschHint().isEmpty()) {
                    String hint = mann.getSpielWunschHint().toLowerCase();
                    if (hint.contains("onn")) { kategorie.setSpielwunsch(SpielTageszeit.SONNTAGMORGEN); hintS = "sonntag"; }
                    if (hint.contains("orge")) { kategorie.setSpielwunsch(SpielTageszeit.SAMSTAGMORGEN); hintS = "morgen"; }
                    if (hint.contains("nach")) { kategorie.setSpielwunsch(SpielTageszeit.SAMSTAGNACHMITTAG); hintS = "nachmittag"; }
                }
                mann.setGruppe(kategorie.getGruppeA());
            }
            if (hintS != null) { for (Mannschaft mann : m) { mann.setSpielWunschHint(hintS); } }
            mannschaftRepo.saveAll(m);
            this.kategorieRepo.save(kategorie);
        }
        for (Kategorie kat : kategorieRepo.findByGame(game)) {
            if (kat.getMannschaften().isEmpty()) { kategorieRepo.delete(kat); }
        }
    }

    private void zuKleineGruppenInAndereKategorieSchieben(Map<String, Kategorie> map, List<String> kKeys) {
        for (int i = 0; i < kKeys.size(); i++) {
            String keyActual = kKeys.get(i);
            Kategorie temp = kategorieRepo.findById(map.get(keyActual).getId()).get();
            if (temp == null || temp.getGruppeA() == null || temp.getGruppeA().getMannschaften().size() == 0) { map.remove(keyActual); }
            boolean moved = false;
            if (temp.getGruppeA().getMannschaften().size() < 3) {
                String keyPlusOne = "";
                try { keyPlusOne = kKeys.get(i + 1); } catch (IndexOutOfBoundsException e) { log.debug("keine hoehere kategorie: " + keyActual); }
                Kategorie kategoriePlusOne = null;
                if (map.get(keyPlusOne) != null) { kategoriePlusOne = kategorieRepo.findById(map.get(keyPlusOne).getId()).get(); }
                if (kategoriePlusOne != null && kategoriePlusOne.getGruppeA().getMannschaften().size() > 0) { moved = plusOneBigger(map, keyActual, temp, kategoriePlusOne); }
                if (!moved) {
                    String keyMinusOne;
                    try { keyMinusOne = kKeys.get(i - 1); } catch (IndexOutOfBoundsException e) { log.debug("keine tiefere kategorie: " + keyActual); continue; }
                    Kategorie kategorieMinusOne = kategorieRepo.findById(map.get(keyMinusOne).getId()).get();
                    if (kategorieMinusOne != null && kategorieMinusOne.getGruppeA().getMannschaften().size() > 0) {
                        List<Mannschaft> manns = temp.getGruppeA().getMannschaften();
                        for (Mannschaft mannschaft : manns) { mannschaft.setGruppe(kategorieMinusOne.getGruppeA()); kategorieMinusOne.getGruppeA().getMannschaften().add(mannschaft); }
                        kategorieMinusOne.getGruppeA().getMannschaften().sort(new MannschaftsNamenComperator());
                        map.remove(keyActual);
                        for (Mannschaft mt : temp.getGruppeA().getMannschaften()) { mt.setGruppe(null); this.mannschaftRepo.save(mt); }
                        for (Mannschaft mt : temp.getGruppeB().getMannschaften()) { mt.setGruppe(null); this.mannschaftRepo.save(mt); }
                        kategorieRepo.delete(temp);
                        if (kategorieMinusOne != null) { kategorieRepo.save(kategorieMinusOne); }
                    }
                }
            }
        }
    }

    private boolean plusOneBigger(Map<String, Kategorie> map, String keyActual, Kategorie temp, Kategorie kategoriePlusOne) {
        List<Mannschaft> manns = temp.getGruppeA().getMannschaften();
        for (Mannschaft mannschaft : manns) { mannschaft.setGruppe(kategoriePlusOne.getGruppeA()); kategoriePlusOne.getGruppeA().getMannschaften().add(mannschaft); }
        kategoriePlusOne.getGruppeA().getMannschaften().sort(new MannschaftsNamenComperator());
        map.remove(keyActual);
        Gruppe a = temp.getGruppeA(); Gruppe b = temp.getGruppeB();
        a.setKategorie(null); b.setKategorie(null);
        a = gruppeRepo.save(a); b = gruppeRepo.save(b);
        temp.setGruppeA(null); temp.setGruppeB(null);
        gruppeRepo.save(a); gruppeRepo.save(b);
        kategorieRepo.save(temp);
        gruppeRepo.deleteById(a.getId()); gruppeRepo.deleteById(b.getId());
        kategorieRepo.save(kategoriePlusOne);
        return true;
    }

    private Map<String, Kategorie> zuordnungVornehmen(List<Mannschaft> mannschaften, String game) {
        Map<String, Kategorie> map = new HashMap<>();
        for (Mannschaft mannschaft : mannschaften) {
            String key = "" + mannschaft.getGeschlecht() + mannschaft.getKlasse();
            Kategorie tempKategorie = map.get(key);
            if (tempKategorie == null) {
                tempKategorie = new Kategorie(); tempKategorie.setGame(game);
                tempKategorie = this.kategorieRepo.save(tempKategorie);
                Gruppe tempGr = new Gruppe(); tempGr.setGeschlecht(mannschaft.getGeschlecht());
                tempGr = this.gruppeRepo.save(tempGr); tempGr.setKategorie(tempKategorie);
                tempKategorie.setGruppeA(tempGr); tempKategorie = this.kategorieRepo.save(tempKategorie);
                gruppeRepo.save(tempGr);
                Gruppe tempGrB = new Gruppe(); tempGrB.setGeschlecht(mannschaft.getGeschlecht());
                tempGrB = this.gruppeRepo.save(tempGrB); tempGrB.setKategorie(tempKategorie);
                tempKategorie.setGruppeB(tempGrB); tempKategorie = this.kategorieRepo.save(tempKategorie);
                gruppeRepo.save(tempGrB);
            }
            tempKategorie.getGruppeA().getMannschaften().add(mannschaftRepo.findById(mannschaft.getId()).get());
            log.info(mannschaft.toString());
            tempKategorie = this.kategorieRepo.save(tempKategorie);
            map.put(key, tempKategorie);
        }
        return map;
    }
}
