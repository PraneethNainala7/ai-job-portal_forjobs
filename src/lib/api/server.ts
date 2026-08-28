import { cookies } from "next/headers";
import type { EmployerJob, Job, JobListResponse } from "@/types/domain";

const API_ORIGIN = process.env.API_ORIGIN ?? "http://localhost:8080";

async function serverFetch(path: string, init?: RequestInit) {
  const store = await cookies();
  const cookie = store.toString();
  return fetch(`${API_ORIGIN}${path}`, {
    ...init,
    headers: {
      ...init?.headers,
      ...(cookie ? { cookie } : {}),
    },
    cache: "no-store",
  });
}

export async function fetchPublicJobs(page = 1, pageSize = 3): Promise<Job[]> {
  const response = await serverFetch(`/api/jobs?page=${page}&pageSize=${pageSize}`);
  if (!response.ok) return [];
  const body = (await response.json()) as JobListResponse;
  return body.items ?? [];
}

export async function fetchPublicJob(id: string): Promise<Job | null> {
  const response = await serverFetch(`/api/jobs/${id}`);
  if (!response.ok) return null;
  return (await response.json()) as Job;
}

export async function fetchEmployerJob(id: string): Promise<EmployerJob | null> {
  const response = await serverFetch(`/api/employer/jobs/${id}`);
  if (!response.ok) return null;
  return (await response.json()) as EmployerJob;
}
