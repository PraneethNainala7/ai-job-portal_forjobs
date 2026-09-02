import type { ApplicationRecord, CandidateProfile, MatchResult, RecommendationResponse, ResumeAnalysis } from "@/types/domain";
import { parseWorkspaceResponse } from "@/lib/api/parse-response";

async function parse<T>(response: Response): Promise<T> {
  return parseWorkspaceResponse(response, "candidate");
}
export function getProfileRequest() {
  return fetch("/api/candidate/profile").then((response) => parse<CandidateProfile>(response));
}

export function saveProfileRequest(profile: Partial<CandidateProfile> & { skills: string[] }) {
  return fetch("/api/candidate/profile", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(profile),
  }).then((response) => parse<CandidateProfile>(response));
}

export function getResumeRequest() {
  return fetch("/api/candidate/resume").then((response) => parse<{ resume: ResumeAnalysis | null }>(response));
}

export function uploadResumeRequest(file: File) {
  const form = new FormData();
  form.set("resume", file);
  return fetch("/api/candidate/resume", { method: "POST", body: form }).then((response) =>
    parse<{ resume: ResumeAnalysis }>(response),
  );
}

export function analyzeResumeRequest() {
  return fetch("/api/ai/resume/analyze", { method: "POST" }).then((response) =>
    parse<{ resume: ResumeAnalysis }>(response),
  );
}

export function getApplicationsRequest() {
  return fetch("/api/candidate/applications").then((response) => parse<{ items: ApplicationRecord[] }>(response));
}

export function applyRequest(jobId: string) {
  return fetch(`/api/jobs/${jobId}/apply`, { method: "POST" }).then((response) => parse<{ application: unknown }>(response));
}

export function recommendRequest() {
  return fetch("/api/ai/jobs/recommend", { method: "POST" }).then((response) =>
    parse<RecommendationResponse>(response),
  );
}

export function matchRequest(jobId: string) {
  return fetch(`/api/ai/jobs/${jobId}/match`, { method: "POST" }).then((response) => parse<MatchResult>(response));
}
