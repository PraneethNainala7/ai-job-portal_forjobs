import { getHomePath, isCandidateRouteAllowed } from "@/config/routes";
import { getFreshSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";

export async function requireActiveCandidatePage() {
  const session = await getFreshSession();
  if (!session) redirect("/login");
  if (session.role !== "CANDIDATE") {
    redirect(getHomePath(session.role, session.accountStatus, { cin: session.cin }));
  }
  return session;
}

export function assertCandidateRouteAllowed(pathname: string, session: NonNullable<Awaited<ReturnType<typeof getFreshSession>>>) {
  if (!isCandidateRouteAllowed(session.accountStatus, pathname)) {
    redirect(getHomePath(session.role, session.accountStatus));
  }
}
