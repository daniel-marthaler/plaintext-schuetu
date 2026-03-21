package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Kategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for {@link Kategorie} instances.
 */
public interface KategorieRepository extends JpaRepository<Kategorie, Long> {

    @Query("select o from Kategorie o where o.gruppeA.geschlecht = 0 and o.game= ?1")
    List<Kategorie> getKategorienMList(String game);

    @Query("select o from Kategorie o where o.gruppeA.geschlecht = 1 and o.game= ?1")
    List<Kategorie> getKategorienKList(String game);

    List<Kategorie> findByGame(String game);
}
