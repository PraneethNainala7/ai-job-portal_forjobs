import type { GoogleAuthResponse, SessionUser, UserRole } from "@/types/domain";

export class AuthRequestError extends Error {
  code?: string;

  constructor(message: string, code?: string) {
    super(message);
    this.code = code;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  const body = (await response.json().catch(() => ({}))) as { error?: string; code?: string };
  if (!response.ok) {
    throw new AuthRequestError(body.error ?? "Request failed.", body.code);
  }
  return body as T;
}

export function loginRequest(email: string, password: string) {
  return request<{ user: SessionUser }>("/api/auth/login", {
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
  return request<{ user: SessionUser }>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function googleAuthRequest(idToken: string, role?: UserRole) {
  return request<GoogleAuthResponse>("/api/auth/google", {
    method: "POST",
    body: JSON.stringify({ idToken, role }),
  });
}

export function employerOnboardingRequest(payload: {
  companyName: string;
  companyInformation: string;
  companyLocation: string;
  companyWebsite?: string;
  cin: string;
}) {
  return request<{ user: SessionUser }>("/api/employer/onboarding", {
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

export function forgotPasswordRequest(email: string) {
  return request<{ ok: true }>("/api/auth/forgot-password", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export function resetPasswordRequest(token: string, password: string) {
  return request<{ ok: true }>("/api/auth/reset-password", {
    method: "POST",
    body: JSON.stringify({ token, password }),
  });
}
