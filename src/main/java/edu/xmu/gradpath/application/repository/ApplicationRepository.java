package edu.xmu.gradpath.application.repository;

import edu.xmu.gradpath.application.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
