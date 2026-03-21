package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Scope("prototype")
@Component
public class DurchfuehrungDataDatabase {

    @Autowired
    private SpielZeilenRepository repo;

    @Setter
    private String gameName;

    public List<SpielZeile> getList1Wartend(int size) {
        List<SpielZeile> result = new ArrayList<>();
        int i = 0;
        while (result.size() < size) {
            Pageable p = PageRequest.of(i, size);
            List<SpielZeile> zeilen = this.repo.findNextZeilen(p, gameName);
            if (zeilen.isEmpty()) {
                break;
            }
            for (SpielZeile z : zeilen) {
                if ((z.getA() != null && !z.getA().isFertigEingetragen()) || (z.getB() != null && !z.getB().isFertigEingetragen()) || (z.getC() != null && !z.getC().isFertigEingetragen())) {
                    result.add(z);
                    if (result.size() == size) {
                        break;
                    }
                }
            }
            i++;
        }
        Collections.reverse(result);
        return result;
    }

    public void setList1Wartend(List<SpielZeile> list1Wartend) {
        this.repo.saveAll(list1Wartend);
    }

    public List<SpielZeile> getList2ZumVorbereiten() {
        return repo.findBZurVorbereitung(gameName);
    }

    public void setList2ZumVorbereiten(List<SpielZeile> list2ZumVorbereiten) {
        this.repo.saveAll(list2ZumVorbereiten);
    }

    public List<SpielZeile> getList3Vorbereitet() {
        return repo.findCVorbereitet(gameName);
    }

    public void setList3Vorbereitet(List<SpielZeile> list3Vorbereitet) {
        this.repo.saveAll(list3Vorbereitet);
    }

    public List<SpielZeile> getList4Spielend() {
        return repo.findDSpielend(gameName);
    }

    public void setList4Spielend(List<SpielZeile> list4Spielend) {
        this.repo.saveAll(list4Spielend);
    }

    public List<SpielZeile> getList5Beendet() {
        return repo.findEBeendet(gameName);
    }

    public void setList5Beendet(List<SpielZeile> list5Beendet) {
        this.repo.saveAll(list5Beendet);
    }

}
