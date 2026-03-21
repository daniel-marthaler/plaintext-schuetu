package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.service.utils.IDGeneratorContainer;
import ch.plaintext.schuetu.service.utils.SysoutHelper;
import ch.plaintext.schuetu.entity.Gruppe;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.KategorieNameComparator;
import ch.plaintext.schuetu.repository.GruppeRepository;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class D4GeneratePaarungenAndSpiele {

    @Autowired private KategorieRepository kategorieRepo;
    @Autowired private SpielRepository spielRepo;
    @Autowired private GruppeRepository gruppeRepo;
    @Autowired private MannschaftRepository mannschaftRepo;

    public void generatPaarungenAndSpiele(String game) {
        int spieleCount = 0;
        List<Kategorie> list = kategorieRepo.findByGame(game);
        list.sort(new KategorieNameComparator());
        for (Kategorie kategorie : list) {
            if (kategorie.getGruppeA() == null) { continue; }
            final List<Mannschaft> a = kategorie.getGruppeA().getMannschaften();
            final List<Mannschaft> b = kategorie.getGruppeB().getMannschaften();
            if (a.size() == 3) { assign(a, true, game); } else if (!b.isEmpty()) { assign(b, false, game); }
            assign(a, false, game);
            kategorie = kategorieRepo.findById(kategorie.getId()).get();
            spieleCount += kategorie.getSpiele().size();
            log.info("paarungen und spiele zu kategorie " + kategorie.getName() + " zugeordnet: " + kategorie.getSpiele().size());
        }
        SysoutHelper.printKategorieList(kategorieRepo.findByGame(game));
        log.info(" zugeordnet total: " + spieleCount);
    }

    private void assign(final List<Mannschaft> mannschaften, boolean toBGruppe, String game) {
        for (int i = 0; i < mannschaften.size(); i++) {
            final Mannschaft kandidat = mannschaften.get(i);
            Gruppe gruppeKandidat = kandidat.getGruppe();
            for (int k = i + 1; k < mannschaften.size(); k++) {
                Spiel spiel = new Spiel(); spiel.setGame(game); spiel.setIdString(IDGeneratorContainer.getNext());
                spiel.setKategorieName(gruppeKandidat.getKategorie().getName()); spiel = spielRepo.save(spiel);
                spiel.setMannschaftA(kandidat); spiel.setMannschaftB(mannschaften.get(k)); spiel = spielRepo.save(spiel);
                if (toBGruppe) { gruppeKandidat = gruppeKandidat.getKategorie().getGruppeB(); }
                List<Spiel> spiele = gruppeKandidat.getSpiele();
                gruppeKandidat.getSpiele().clear(); gruppeKandidat = this.gruppeRepo.save(gruppeKandidat);
                for (Spiel sp : spiele) { gruppeKandidat.getSpiele().add(sp); }
                gruppeKandidat.getSpiele().add(spiel);
                this.gruppeRepo.save(gruppeKandidat);
            }
        }
        mannschaftRepo.saveAll(mannschaften);
    }
}
