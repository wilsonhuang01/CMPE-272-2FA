package com.wilson.cmpe272.repository;

import com.wilson.cmpe272.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.emailVerificationCode = :code AND u.emailVerificationExpiresAt > :now")
    Optional<User> findByEmailAndEmailVerificationCode(@Param("email") String email, 
                                                      @Param("code") String code, 
                                                      @Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.phoneVerificationCode = :code AND u.phoneVerificationExpiresAt > :now")
    Optional<User> findByPhoneNumberAndPhoneVerificationCode(@Param("phoneNumber") String phoneNumber, 
                                                            @Param("code") String code, 
                                                            @Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isEmailVerified = true")
    Optional<User> findByEmailAndEmailVerified(@Param("email") String email);
}
