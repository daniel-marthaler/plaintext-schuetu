package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Gruppe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link Gruppe} instances.
 */
public interface GruppeRepository extends JpaRepository<Gruppe, Long> {

    java.util.List<Gruppe> findByGame(String game);
}
