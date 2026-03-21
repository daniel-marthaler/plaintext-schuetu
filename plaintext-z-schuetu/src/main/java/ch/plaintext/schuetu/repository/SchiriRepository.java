package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Schiri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for {@link Schiri} instances.
 */
public interface SchiriRepository extends JpaRepository<Schiri, Long> {

    List<Schiri> findByGame(String game);

    List<Schiri> findByGameAndAktiviert(String game, Boolean aktiv);

    @Query("select distinct o.einteilung from Schiri o where o.einteilung like %?1% and o.game like ?2")
    List<String> findAllEinteilungen(String query, String game);

}
