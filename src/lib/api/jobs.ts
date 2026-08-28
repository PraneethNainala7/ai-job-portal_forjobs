import type { Job, JobListResponse } from "@/types/domain";

export async function fetchJobs(params: Record<string, string>) {
  const query = new URLSearchParams(params);
  const response = await fetch(`/api/jobs?${query.toString()}`);
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.error ?? "Could not load jobs.");
  return body as JobListResponse;
}

export async function fetchJob(id: string) {
  const response = await fetch(`/api/jobs/${id}`);
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.error ?? "Job not found.");
  return body as Job;
}
