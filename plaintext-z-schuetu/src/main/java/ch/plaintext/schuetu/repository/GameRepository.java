package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.GameModel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Game JPA Repo
 */
public interface GameRepository extends JpaRepository<GameModel, Long> {

    GameModel findByGameName(String gameName);

}
