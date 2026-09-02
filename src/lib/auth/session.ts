import { cookies } from "next/headers";
import type { SessionUser } from "@/types/domain";

export const SESSION_COOKIE = "aip_session";

const API_ORIGIN = process.env.API_ORIGIN ?? "http://localhost:8080";

function sessionCookieHeader(token: string) {
  return `${SESSION_COOKIE}=${token}`;
}

function decodeBase64Url(value: string) {
  const padded = value.replace(/-/g, "+").replace(/_/g, "/") + "==".slice(0, (4 - (value.length % 4)) % 4);
  return atob(padded);
}

function parseJwt(value: string): SessionUser | null {
  const parts = value.split(".");
  if (parts.length !== 3) return null;
  try {
    const payload = JSON.parse(decodeBase64Url(parts[1])) as Record<string, string | number | undefined>;
    if (typeof payload.exp === "number" && payload.exp * 1000 <= Date.now()) {
      return null;
    }
    if (!payload.sub || !payload.role || !payload.accountStatus) return null;
    return {
      id: String(payload.sub),
      name: String(payload.name ?? ""),
      email: String(payload.email ?? ""),
      role: String(payload.role) as SessionUser["role"],
      accountStatus: String(payload.accountStatus) as SessionUser["accountStatus"],
      companyName: payload.companyName ? String(payload.companyName) : undefined,
      companyInformation: payload.companyInformation ? String(payload.companyInformation) : undefined,
      companyLocation: payload.companyLocation ? String(payload.companyLocation) : undefined,
      cin: payload.cin ? String(payload.cin) : undefined,
      companyWebsite: payload.companyWebsite ? String(payload.companyWebsite) : undefined,
      rejectionReason: payload.rejectionReason ? String(payload.rejectionReason) : undefined,
    };
  } catch {
    return null;
  }
}

export function parseSessionValue(value?: string): SessionUser | null {
  if (!value) return null;
  try {
    const raw = decodeURIComponent(value);
    if (raw.split(".").length === 3) return parseJwt(raw);
    const parsed = JSON.parse(raw) as SessionUser;
    if (!parsed.id || !parsed.role || !parsed.accountStatus) return null;
    return parsed;
  } catch {
    return parseJwt(value);
  }
}

export async function getSession(): Promise<SessionUser | null> {
  const store = await cookies();
  return parseSessionValue(store.get(SESSION_COOKIE)?.value);
}

export async function clearSessionCookie() {
  const store = await cookies();
  store.delete(SESSION_COOKIE);
}

export async function getFreshSession(): Promise<SessionUser | null> {
  const store = await cookies();
  const existing = store.get(SESSION_COOKIE)?.value;
  if (!existing) return null;

  try {
    const response = await fetch(`${API_ORIGIN}/api/auth/session`, {
      headers: { cookie: sessionCookieHeader(existing) },
      cache: "no-store",
    });

    if (response.ok) {
      const body = (await response.json()) as { user: SessionUser | null };
      if (body.user?.id && body.user.role && body.user.accountStatus) {
        return body.user;
      }
      await clearSessionCookie();
      return null;
    }

    if (response.status === 401) {
      await clearSessionCookie();
      return null;
    }
  } catch {
    // Fall back when the backend is unreachable.
  }

  return parseSessionValue(existing);
}
