type Workspace = "employer" | "candidate";

const RESTRICTED_PATH: Record<Workspace, string> = {
  employer: "/employer/restricted",
  candidate: "/candidate/restricted",
};

function isAccountRestriction(status: number, message?: string) {
  if (!message) return status === 403;
  const lower = message.toLowerCase();
  if (status === 403) {
    return (
      lower.includes("hiring tools") ||
      lower.includes("candidate features") ||
      lower.includes("inactive") ||
      lower.includes("do not have access")
    );
  }
  if (status !== 401) return false;
  return lower.includes("inactive") || lower.includes("sign-in required");
}

export function redirectToRestrictedWorkspace(workspace: Workspace) {
  if (typeof window === "undefined") return;
  const target = RESTRICTED_PATH[workspace];
  if (window.location.pathname === target) return;
  window.location.assign(target);
}

export async function parseWorkspaceResponse<T>(response: Response, workspace: Workspace): Promise<T> {
  const body = (await response.json().catch(() => ({}))) as { error?: string };
  if (isAccountRestriction(response.status, body.error)) {
    redirectToRestrictedWorkspace(workspace);
    throw new Error(body.error ?? "Account restricted.");
  }
  if (!response.ok) throw new Error(body.error ?? "Request failed.");
  return body as T;
}
