package com.whatsapp.service.repository;

import com.whatsapp.service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findByPhoneNumber(String phoneNumber);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession u WHERE u.lastActivity < :expiredTime")
    void deleteExpiredSessions(@Param("expiredTime") LocalDateTime expiredTime);
    
    @Modifying
    @Transactional
    void deleteByPhoneNumber(String phoneNumber);
}
