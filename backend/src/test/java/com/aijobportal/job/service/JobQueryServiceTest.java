package com.aijobportal.job.service;

import com.aijobportal.application.repository.JobApplicationRepository;
import com.aijobportal.auth.entity.User;
import com.aijobportal.common.domain.AccountStatus;
import com.aijobportal.common.domain.JobStatus;
import com.aijobportal.common.domain.Role;
import com.aijobportal.job.entity.Job;
import com.aijobportal.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobQueryServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;

    private JobQueryService service;

    @BeforeEach
    void setUp() {
        service = new JobQueryService(jobRepository, jobApplicationRepository);
    }

    @Test
    void listAllActiveUsesPublicActiveEmployerQuery() {
        User employer = employer(AccountStatus.ACTIVE);
        Job visible = job("job-1", employer);
        when(jobRepository.findPublicActiveJobsOrderByPostedDateDesc(JobStatus.ACTIVE, AccountStatus.ACTIVE))
                .thenReturn(List.of(visible));

        var items = service.listAllActive();

        assertEquals(1, items.size());
        assertEquals("job-1", items.getFirst().id());
    }

    @Test
    void searchUsesPublicActiveEmployerQuery() {
        User employer = employer(AccountStatus.ACTIVE);
        Job visible = job("job-1", employer);
        when(jobRepository.findPublicActiveJobsOrderByPostedDateDesc(JobStatus.ACTIVE, AccountStatus.ACTIVE))
                .thenReturn(List.of(visible));

        var result = service.search(null, null, null, null, null, null, null, 1, 6);

        assertEquals(1, result.total());
        assertEquals("job-1", result.items().getFirst().id());
    }

    private static User employer(AccountStatus status) {
        User user = new User();
        user.setId("employer-1");
        user.setName("Employer");
        user.setEmail("employer@test.com");
        user.setRole(Role.EMPLOYER);
        user.setAccountStatus(status);
        return user;
    }

    private static Job job(String id, User employer) {
        Job job = new Job();
        job.setId(id);
        job.setEmployer(employer);
        job.setStatus(JobStatus.ACTIVE);
        job.setRole("Engineer");
        job.setCompanyName("Acme");
        job.setExperience("3+ years");
        job.setSkills(List.of("Java"));
        job.setLocation("Remote");
        job.setSalary("100k");
        job.setJobType("Full-time");
        job.setDescription("Build things");
        return job;
    }
}
