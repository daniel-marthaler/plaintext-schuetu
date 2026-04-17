package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.MqttConsumedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MqttConsumedMessageRepository extends JpaRepository<MqttConsumedMessage, Long> {

    boolean existsByHash(String hash);

    List<MqttConsumedMessage> findByGameOrderByReceivedAtAsc(String game);
}
