import { cookies } from "next/headers";
import type { SessionUser } from "@/types/domain";

export const SESSION_COOKIE = "aip_session";

function decodeBase64Url(value: string) {
  const padded = value.replace(/-/g, "+").replace(/_/g, "/") + "==".slice(0, (4 - (value.length % 4)) % 4);
  return atob(padded);
}

function parseJwt(value: string): SessionUser | null {
  const parts = value.split(".");
  if (parts.length !== 3) return null;
  try {
    const payload = JSON.parse(decodeBase64Url(parts[1])) as Record<string, string | undefined>;
    if (!payload.sub || !payload.role || !payload.accountStatus) return null;
    return {
      id: payload.sub,
      name: payload.name ?? "",
      email: payload.email ?? "",
      role: payload.role as SessionUser["role"],
      accountStatus: payload.accountStatus as SessionUser["accountStatus"],
      companyName: payload.companyName,
      companyInformation: payload.companyInformation,
      companyLocation: payload.companyLocation,
      cin: payload.cin,
      companyWebsite: payload.companyWebsite,
      rejectionReason: payload.rejectionReason,
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
