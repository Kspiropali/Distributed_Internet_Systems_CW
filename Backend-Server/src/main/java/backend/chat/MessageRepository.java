package backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.type = ?1")
    Optional<Message> findMessageByType(String type);

    @Query("select m from Message m where m.destination in ?1")
    List<Message> findAllByDestination(String destination);

    @Transactional
    @Modifying
    @Query("delete from Message m where m.sender = ?1")
    void deleteAllBySender(String sender);
}

