package edu.xmu.gradpath.review.service;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.domain.ApplicationStatus;
import edu.xmu.gradpath.application.repository.ApplicationRepository;
import edu.xmu.gradpath.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ApplicationRepository applicationRepository;

    public ReviewService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * 对 Application 进行一次审核
     *
     * @param applicationId 被审核的申请 id
     * @param approved      是否通过
     */
    @Transactional
    public void review(Long applicationId, boolean approved) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new BizException(404, "application not found")
                );

        // 只能审核已提交的申请
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new BizException(
                    400,
                    "only submitted application can be reviewed"
            );
        }

        if (approved) {
            application.markApproved();
        } else {
            application.markRejected();
        }

        applicationRepository.save(application);
    }
}
