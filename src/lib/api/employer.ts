import type {
  EmployerApplicant,
  EmployerJob,
  InterviewQuestions,
  Job,
  SessionUser,
} from "@/types/domain";
import { parseWorkspaceResponse } from "@/lib/api/parse-response";

async function parse<T>(response: Response): Promise<T> {
  return parseWorkspaceResponse(response, "employer");
}

export function getEmployerProfileRequest() {
  return fetch("/api/employer/profile").then((response) => parse<SessionUser>(response));
}

export function saveEmployerProfileRequest(input: {
  companyName: string;
  companyInformation: string;
  companyLocation: string;
  companyWebsite?: string;
}) {
  return fetch("/api/employer/profile", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  }).then((response) => parse<SessionUser>(response));
}

export function resubmitEmployerRequest() {
  return fetch("/api/employer/resubmit", { method: "POST" }).then((response) => parse<SessionUser>(response));
}

export function getEmployerDashboardRequest() {
  return fetch("/api/employer/dashboard").then((response) =>
    parse<{ jobs: number; activeJobs: number; applicants: number; shortlisted: number; interviews: number }>(response),
  );
}

export function getEmployerJobsRequest() {
  return fetch("/api/employer/jobs").then((response) => parse<{ items: EmployerJob[] }>(response));
}

export function getEmployerJobRequest(id: string) {
  return fetch(`/api/employer/jobs/${id}`).then((response) => parse<EmployerJob>(response));
}

export function createEmployerJobRequest(input: unknown) {
  return fetch("/api/employer/jobs", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  }).then((response) => parse<Job>(response));
}

export function updateEmployerJobRequest(id: string, input: unknown) {
  return fetch(`/api/employer/jobs/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  }).then((response) => parse<Job>(response));
}

export function closeEmployerJobRequest(id: string) {
  return fetch(`/api/employer/jobs/${id}/close`, { method: "POST" }).then((response) => parse<Job>(response));
}

export function getApplicantsRequest(jobId: string) {
  return fetch(`/api/employer/jobs/${jobId}/applicants`).then((response) =>
    parse<{ items: EmployerApplicant[] }>(response),
  );
}

export function shortlistRequest(applicationId: string) {
  return fetch(`/api/employer/applications/${applicationId}/shortlist`, { method: "PUT" }).then((response) =>
    parse<unknown>(response),
  );
}

export function rejectRequest(applicationId: string, reason?: string) {
  return fetch(`/api/employer/applications/${applicationId}/reject`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ reason }),
  }).then((response) => parse<unknown>(response));
}

export function interviewQuestionsRequest(jobId: string, candidateId: string) {
  return fetch("/api/ai/interview-questions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ jobId, candidateId }),
  }).then((response) => parse<{ questions: InterviewQuestions; disclaimer: string }>(response));
}
