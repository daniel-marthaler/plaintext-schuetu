package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Spiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for {@link Spiel} instances.
 */
public interface SpielRepository extends JpaRepository<Spiel, Long> {

    @Query("select o from Spiel o where o.typ = 0 and o.game= ?1")
    List<Spiel> findGruppenSpiel(String game);

    @Query("select o from Spiel o where o.typ = 1 or o.typ = 2 and o.game= ?1")
    List<Spiel> findFinalSpiel(String game);

    @Query("select o from Spiel o where o.mannschaftA.id = ?1 or o.mannschaftB.id = ?1")
    List<Spiel> findSpielFromMannschaft(Long id);

    @Query("select o from Spiel o where (o.typ = 0 and o.game= ?1) order by o.start,o.platz  asc")
    List<Spiel> findGruppenSpielAsc(String game);

    @Query("select o from Spiel o where (o.typ = 1 or o.typ = 2) and o.game= ?1  order by o.start, o.platz asc")
    List<Spiel> findFinalSpielAsc(String game);

    @Query("select o from Spiel o where o.fertigGespielt = TRUE and o.fertigEingetragen = FALSE  and o.game= ?1 order by o.start asc")
    List<Spiel> findAllEinzutragende(String game);

    @Query("select o from Spiel o where o.fertigGespielt = TRUE and o.fertigEingetragen = TRUE and o.fertigBestaetigt = FALSE and o.game= ?1")
    List<Spiel> findAllZuBestaetigen(String game);

    @Query("select o from Spiel o where o.idString = ?1 and o.game= ?2")
    Spiel findSpielByIdString(String idString, String game);

    List<Spiel> findByGame(String game);

}
