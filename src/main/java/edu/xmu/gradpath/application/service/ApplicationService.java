package edu.xmu.gradpath.application.service;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.repository.ApplicationRepository;
import edu.xmu.gradpath.common.exception.BizException;
import edu.xmu.gradpath.application.domain.ApplicationStatus;
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
    public Long createDraft(Long userId) {

        applicationRepository
                .findByUserIdAndStatus(userId, ApplicationStatus.DRAFT)
                .ifPresent(existing -> {
                    throw new BizException(
                            400,
                            "draft application already exists"
                    );
                });

        Application draft = Application.createDraft(userId);
        applicationRepository.save(draft);
        return draft.getId();
    }


    /**
     * 提交申请：DRAFT -> SUBMITTED
     */
    public Long submit(Long userId, Long applicationId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new BizException(404, "application not found")
                );

        // MVP 阶段：不做鉴权体系，但至少防止提交他人申请
        if (!application.getUserId().equals(userId)) {
            throw new BizException(403, "not your application");
        }

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new BizException(
                    400,
                    "only draft application can be submitted"
            );
        }

        application.markSubmitted();

        applicationRepository.save(application);

        return application.getId();
    }
    /**
     * 查询 Application（只读）
     */
    public Application getById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new BizException(404, "application not found")
                );
    }

}
