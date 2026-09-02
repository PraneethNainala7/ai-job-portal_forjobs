import { cookies } from "next/headers";
import { SESSION_COOKIE } from "@/lib/auth/session";
import type { ApplicationRecord, EmployerJob, Job, JobListResponse } from "@/types/domain";

const API_ORIGIN = process.env.API_ORIGIN ?? "http://localhost:8080";

export type FetchListResult<T> = { ok: true; items: T[] } | { ok: false; items: [] };

async function serverFetch(path: string, init?: RequestInit) {
  const store = await cookies();
  const token = store.get(SESSION_COOKIE)?.value;
  try {
    return await fetch(`${API_ORIGIN}${path}`, {
      ...init,
      headers: {
        ...init?.headers,
        ...(token ? { cookie: `${SESSION_COOKIE}=${token}` } : {}),
      },
      cache: "no-store",
    });
  } catch {
    return null;
  }
}

export async function fetchPublicJobs(page = 1, pageSize = 3): Promise<Job[]> {
  const result = await fetchPublicJobsResult(page, pageSize);
  return result.items;
}

export async function fetchPublicJobsResult(
  page = 1,
  pageSize = 3,
): Promise<FetchListResult<Job & { matchScore?: number }>> {
  const response = await serverFetch(`/api/jobs?page=${page}&pageSize=${pageSize}`);
  if (!response?.ok) return { ok: false, items: [] };
  const body = (await response.json()) as JobListResponse;
  return { ok: true, items: body.items ?? [] };
}

export async function fetchEmployerJobs(): Promise<FetchListResult<EmployerJob>> {
  const response = await serverFetch("/api/employer/jobs");
  if (!response?.ok) return { ok: false, items: [] };
  const body = (await response.json()) as { items?: EmployerJob[] };
  return { ok: true, items: body.items ?? [] };
}

export async function fetchCandidateApplications(): Promise<FetchListResult<ApplicationRecord>> {
  const response = await serverFetch("/api/candidate/applications");
  if (!response?.ok) return { ok: false, items: [] };
  const body = (await response.json()) as { items?: ApplicationRecord[] };
  return { ok: true, items: body.items ?? [] };
}

export async function fetchPublicJob(id: string): Promise<Job | null> {
  const response = await serverFetch(`/api/jobs/${id}`);
  if (!response?.ok) return null;
  return (await response.json()) as Job;
}

export async function fetchEmployerJob(id: string): Promise<EmployerJob | null> {
  const response = await serverFetch(`/api/employer/jobs/${id}`);
  if (!response?.ok) return null;
  return (await response.json()) as EmployerJob;
}
