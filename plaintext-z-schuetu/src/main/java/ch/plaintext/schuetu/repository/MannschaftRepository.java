package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Mannschaft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for {@link Mannschaft} instances.
 */
public interface MannschaftRepository extends JpaRepository<Mannschaft, Long> {

    @Query("select o from Mannschaft o where o.begleitpersonTelefon = ?1")
    Mannschaft findBybegleitpersonTelefon(String telefon);

    @Query("select o from Mannschaft o where o.id = ?1")
    Mannschaft findByStringId(String telefon);

    List<Mannschaft> findByGame(String game);

}
