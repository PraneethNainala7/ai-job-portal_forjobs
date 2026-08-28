import type { AuthResponse, SessionUser, UserRole } from "@/types/domain";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(body.error ?? "Request failed.");
  }
  return body as T;
}

export function loginRequest(email: string, password: string) {
  return request<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function registerRequest(payload: {
  name: string;
  email: string;
  password: string;
  role: UserRole;
  companyName?: string;
  companyInformation?: string;
  companyLocation?: string;
  cin?: string;
}) {
  return request<AuthResponse>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function logoutRequest() {
  return request<{ ok: true }>("/api/auth/logout", { method: "POST" });
}

export function sessionRequest() {
  return request<{ user: SessionUser | null }>("/api/auth/session");
}
