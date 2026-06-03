package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    Optional<ChatRoom> findByCustomerIdAndStatus(String customerId, String status);

    List<ChatRoom> findAllByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.status = 'ACTIVE' ORDER BY cr.createdAt DESC")
    List<ChatRoom> findAllActiveRooms();
}
