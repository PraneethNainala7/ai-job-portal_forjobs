import type { ApplicationStatus } from "@/types/domain";

export function hasAppliedToJob(applications: { jobId: string }[], jobId: string): boolean {
  return applications.some((item) => item.jobId === jobId);
}

export function canShortlistApplication(status: ApplicationStatus): boolean {
  return status === "APPLIED";
}

export function canRejectApplication(status: ApplicationStatus): boolean {
  return status === "APPLIED" || status === "SHORTLISTED" || status === "INTERVIEW";
}

export function isClosedApplication(status: ApplicationStatus): boolean {
  return status === "REJECTED" || status === "SELECTED";
}

export function canGenerateInterviewQuestions(status: ApplicationStatus): boolean {
  return status === "APPLIED" || status === "SHORTLISTED";
}
