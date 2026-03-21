package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for {@link Penalty} instances.
 */
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    @Query("select o from Penalty o where o.reihenfolgeOrig = ?1 and o.game = ?2")
    Penalty findPenaltyByOriginalreihenfolge(String reihenfolge, String game);

    List<Penalty> findByGame(String game);

}
