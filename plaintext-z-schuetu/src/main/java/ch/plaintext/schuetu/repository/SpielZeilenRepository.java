package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.SpielZeile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for {@link SpielZeile} instances.
 */
public interface SpielZeilenRepository extends JpaRepository<SpielZeile, Long> {

    @Query(value = "select o from SpielZeile o where o.finale = true and o.game= ?1 order by o.start asc ")
    List<SpielZeile> findFinalSpielZeilen(String game);

    @Query("select o from SpielZeile o where o.finale = false and o.game= ?1 order by o.start asc ")
    List<SpielZeile> findGruppenSpielZeilen(String game);

    @Query("select o from SpielZeile o where o.sonntag = true and o.game= ?1 order by o.start asc")
    List<SpielZeile> findSpieleSonntag(String game);

    @Query("select o from SpielZeile o where o.sonntag = false and o.game= ?1 order by o.start asc")
    List<SpielZeile> findSpieleSamstag(String game);

    @Query("select o from SpielZeile o where o.phase = 0 and (o.a is not null or o.b is not null or o.c is not null) and o.game= ?1 order by o.start asc")
    List<SpielZeile> findNextZeilen(Pageable pageable, String game);

    @Query("select o from SpielZeile o where o.phase = 1 and o.game= ?1 order by o.start asc")
    List<SpielZeile> findBZurVorbereitung(String game);

    @Query("select o from SpielZeile o where o.phase = 2 and o.game= ?1 order by o.start asc")
    List<SpielZeile> findCVorbereitet(String game);

    @Query("select o from SpielZeile o where o.phase = 3 and o.game= ?1 order by o.start asc")
    List<SpielZeile> findDSpielend(String game);

    @Query("select o from SpielZeile o where o.phase = 4 and o.game= ?1 order by o.start asc")
    List<SpielZeile> findEBeendet(String game);

    List<SpielZeile> findByGame(String game);

}
