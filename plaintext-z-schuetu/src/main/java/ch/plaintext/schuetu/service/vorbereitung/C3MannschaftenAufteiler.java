package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.service.utils.IDGeneratorContainer;
import ch.plaintext.schuetu.service.vorbereitung.helper.SpielRealNameHelperVorSpielzuteilung;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.MannschaftsNameComparator;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class C3MannschaftenAufteiler {

    @Autowired private KategorieRepository kategorieRepo;
    @Autowired private MannschaftRepository mannschaftsRepo;
    @Autowired private SpielRepository spielRepo;

    public void mannschaftenVerteilen(String game, GameModel model) {
        Iterable<Kategorie> kat = kategorieRepo.findByGame(game);
        List<Long> kategorieIDs = new ArrayList<>();
        for (Kategorie kategorie : kat) { kategorieIDs.add(kategorie.getId()); }
        for (Long id : kategorieIDs) {
            Kategorie kategorie = kategorieRepo.findById(id).get();
            if (kategorie.getGruppeA() == null) { continue; }
            int mannschaftenSize = kategorie.getGruppeA().getMannschaften().size();
            if (mannschaftenSize < 3) { log.error("!!! weniger als 3 mannschaften"); }
            else if (mannschaftenSize == 3) { kategorie = genau3Mannschaften(kategorie, model); }
            else if (mannschaftenSize > 7) { kategorie = groesserAls7Mannschaften(kategorie, mannschaftenSize, model); }
            else { dreiBis7Mannschaften(kategorie, model); }
            kategorieRepo.save(kategorie);
        }
        Iterable<Kategorie> katn = kategorieRepo.findByGame(game);
        for (Kategorie k : katn) {
            int i = 1;
            for (Mannschaft m : k.getMannschaften()) { m.setTeamNummer(i); i++; mannschaftsRepo.save(m); }
        }
    }

    private void dreiBis7Mannschaften(Kategorie kategorie, GameModel model) {
        Spiel gf = new Spiel(); gf.setGame(model.getGameName()); gf.setTyp(SpielEnum.GFINAL);
        gf.setIdString(IDGeneratorContainer.getNext()); gf.setKategorieName(kategorie.getName());
        SpielRealNameHelperVorSpielzuteilung.setRealGross(kategorie, model, gf);
        gf = spielRepo.save(gf); kategorie.setGrosserFinal(gf);
        if (kategorie.computeAnzahlFinale() > 1) {
            Spiel kf = new Spiel(); kf.setGame(model.getGameName());
            SpielRealNameHelperVorSpielzuteilung.setRealNameKlein(kategorie, model, kf);
            if (kategorie.isMixedAndWithEinzelklasse()) {
                kf.setRealName("KlFin-" + kategorie.getMannschaften().get(0).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse());
            }
            kf.setIdString(IDGeneratorContainer.getNext()); kf.setKategorieName(kategorie.getName());
            kf = spielRepo.save(kf); kategorie.setKleineFinal(kf);
        }
        if (kategorie.computeAnzahlFinale() > 2) {
            Spiel gf2 = new Spiel(); gf2.setGame(model.getGameName()); gf2.setTyp(SpielEnum.GFINAL);
            gf2.setIdString(IDGeneratorContainer.getNext()); gf2.setKategorieName(kategorie.getName());
            gf2.setKlasse(" (Kl. " + kategorie.getKleinereMannschaftsGruppe().get(0).getKlasse() + ")");
            gf2.setRealName("GrFin-" + kategorie.getGroessereMannschaftsGruppe().get(0).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse() + "&" + kategorie.getKleinereMannschaftsGruppe().get(0).getKlasse() + gf2.getKlasse());
            gf2 = spielRepo.save(gf2); kategorie.setGrosserfinal2(gf2);
            kategorie = kategorieRepo.save(kategorie); spielRepo.save(gf2);
        }
        kategorieRepo.save(kategorie);
    }

    private Kategorie genau3Mannschaften(Kategorie kategorieIn, GameModel model) {
        Kategorie kategorie = kategorieIn;
        Spiel gf = new Spiel(); gf.setGame(model.getGameName()); gf.setTyp(SpielEnum.GFINAL);
        gf.setIdString(IDGeneratorContainer.getNext()); gf.setKategorieName(kategorie.getName());
        gf = spielRepo.save(gf); kategorie.setGrosserFinal(gf);
        kategorie = kategorieRepo.save(kategorie);
        return kategorie;
    }

    private Kategorie groesserAls7Mannschaften(Kategorie kategorie, int mannschaftenSize, GameModel model) {
        kategorie = kategorieRepo.findById(kategorie.getId()).get();
        List<Mannschaft> kandidatenA = new ArrayList<>(); List<Mannschaft> kandidatenB = new ArrayList<>();
        List<Mannschaft> tempTeil = new ArrayList<>(kategorie.getGruppeA().getMannschaften());
        List<Mannschaft> tempTeil2 = new ArrayList<>(kategorie.getGruppeA().getMannschaften());
        boolean esHatNoch = true;
        while (esHatNoch && !tempTeil.isEmpty()) {
            Mannschaft m1 = tempTeil.remove(0);
            for (Mannschaft m2 : tempTeil) {
                if (m1.getGr() != null && !m1.getGr().isEmpty() && m1.getGr().equalsIgnoreCase("a")) { kandidatenA.add(m1); tempTeil2.remove(m1); }
                else if (m1.getGr() != null && !m1.getGr().isEmpty() && m1.getGr().equalsIgnoreCase("b")) { kandidatenB.add(m1); tempTeil2.remove(m1); }
                else if (m1.getSchulhaus().equals(m2.getSchulhaus())) { kandidatenA.add(m1); kandidatenB.add(m2); tempTeil2.remove(m1); tempTeil2.remove(m2); }
                if (tempTeil.size() < 2) { esHatNoch = false; }
                break;
            }
        }
        List<Mannschaft> tempTeil3 = new ArrayList<>(tempTeil2);
        for (Mannschaft temp : tempTeil3) {
            Mannschaft m = tempTeil2.remove(0);
            if (m.getGr() != null && m.getGr().toUpperCase().equalsIgnoreCase("a")) { kandidatenA.add(m); }
            else if (m.getGr() != null && m.getGr().toUpperCase().equalsIgnoreCase("b")) { kandidatenB.add(m); }
            else { if (kandidatenA.size() < kandidatenB.size()) { kandidatenA.add(m); } else { kandidatenB.add(m); } }
        }
        for (Mannschaft mtemp : kandidatenB) {
            int i = kategorie.getGruppeA().getMannschaften().indexOf(mtemp);
            Mannschaft mm = kategorie.getGruppeA().getMannschaften().remove(i);
            mm.setGruppe(kategorie.getGruppeB()); kategorie.getGruppeB().getMannschaften().add(mm);
        }
        kategorie.getGruppeB().getMannschaften().sort(new MannschaftsNameComparator());
        kategorie.getGruppeA().getMannschaften().sort(new MannschaftsNameComparator());
        kategorie = kategorieRepo.save(kategorie);
        dreiBis7Mannschaften(kategorie, model);
        kategorie = kategorieRepo.save(kategorie);
        return kategorie;
    }
}
