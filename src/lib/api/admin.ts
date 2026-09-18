import type {
  AdminAccount,
  AdminApplication,
  AdminJob,
  AdminStats,
  AuditLog,
  CandidateProfile,
} from "@/types/domain";

async function parse<T>(response: Response): Promise<T> {
  const body = await response.json().catch(() => ({}));
  if (!response.ok) { log.error('Request failed with status: ' + response.status + ' - ' + body.error); throw new Error(body.error ?? 'Request failed.'); }
  return body as T;
}

export function getAdminDashboardRequest() {
  return fetch("/api/admin/dashboard").then((response) =>
    parse<{
      stats: AdminStats;
      recentJobs: AdminJob[];
      recentApplications: AdminApplication[];
      recentAudit: AuditLog[];
      aiUsage: Record<string, number>;
    }>(response),
  );
}

export function getAdminCandidatesRequest() {
  return fetch("/api/admin/candidates").then((response) =>
    parse<{ items: Array<AdminAccount & { title?: string; location?: string; experience?: string }> }>(response),
  );
}

export function getAdminCandidateRequest(id: string) {
  return fetch(`/api/admin/candidates/${id}`).then((response) =>
    parse<{
      account: AdminAccount;
      profile: CandidateProfile;
      resumeSummary: { fileName: string; uploadedAt: string; skills: string[]; status: string } | null;
    }>(response),
  );
}

export function getAdminEmployersRequest() {
  return fetch("/api/admin/employers").then((response) => parse<{ items: AdminAccount[] }>(response));
}

export function getPendingEmployersRequest() {
  return fetch("/api/admin/employers/pending").then((response) => parse<{ items: AdminAccount[] }>(response));
}

export function getAdminEmployerRequest(id: string) {
  return fetch(`/api/admin/employers/${id}`).then((response) => parse<AdminAccount>(response));
}

export function getAdminJobsRequest() {
  return fetch("/api/admin/jobs").then((response) => parse<{ items: AdminJob[] }>(response));
}

export function getAdminJobRequest(id: string) {
  return fetch(`/api/admin/jobs/${id}`).then((response) => parse<AdminJob>(response));
}

export function getAdminApplicationsRequest() {
  return fetch("/api/admin/applications").then((response) => parse<{ items: AdminApplication[] }>(response));
}

export function getAdminAuditRequest() {
  return fetch("/api/admin/audit").then((response) =>
    parse<{ items: AuditLog[]; aiUsage: Record<string, number> }>(response),
  );
}

export function adminPut(path: string, body?: unknown) {
  return fetch(path, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  }).then((response) => parse<unknown>(response));
}

export function adminDelete(path: string) {
  return fetch(path, { method: "DELETE" }).then((response) => parse<{ ok: boolean }>(response));
}
