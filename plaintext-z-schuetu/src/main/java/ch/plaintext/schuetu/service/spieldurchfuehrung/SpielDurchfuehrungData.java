package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.entity.SpielZeile;

import java.util.List;

public interface SpielDurchfuehrungData {

    List<SpielZeile> getList1Wartend(int size);
    void setList1Wartend(List<SpielZeile> list1Wartend);
    List<SpielZeile> getList2ZumVorbereiten();
    void setList2ZumVorbereiten(List<SpielZeile> list2ZumVorbereiten);
    List<SpielZeile> getList3Vorbereitet();
    void setList3Vorbereitet(List<SpielZeile> list3Vorbereitet);
    List<SpielZeile> getList4Spielend();
    void setList4Spielend(List<SpielZeile> list4Spielend);
    List<SpielZeile> getList5Beendet();
    void setList5Beendet(List<SpielZeile> list5Beendet);
    void setGameName(String game);

}
