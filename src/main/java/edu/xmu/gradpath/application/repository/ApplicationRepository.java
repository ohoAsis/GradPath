package edu.xmu.gradpath.application.repository;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.domain.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);
}
