package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Korrektur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for {@link Korrektur} instances.
 */
public interface KorrekturRepository extends JpaRepository<Korrektur, Long> {

    List<Korrektur> findByGame(String game);

}
