package edu.xmu.gradpath.application.service;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * 创建 Application 草稿
     */
    @Transactional
    public Application createDraft(Long userId) {
        Application draft = Application.createDraft(userId);
        return applicationRepository.save(draft);
    }
}
