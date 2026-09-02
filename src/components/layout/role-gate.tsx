import { AppShell } from "@/components/layout/app-shell";
import { getHomePath, isCandidateRouteAllowed, isEmployerRouteAllowed } from "@/config/routes";
import { clearSessionCookie, getFreshSession } from "@/lib/auth/session";
import { headers } from "next/headers";
import { redirect } from "next/navigation";
import type { UserRole } from "@/types/domain";

export async function RoleGate({
  role,
  children,
}: {
  role: UserRole;
  children: React.ReactNode;
}) {
  const session = await getFreshSession();
  if (!session) {
    await clearSessionCookie();
    redirect("/login");
  }  if (session.role !== role) redirect(getHomePath(session.role, session.accountStatus, { cin: session.cin }));

  const pathname = (await headers()).get("x-pathname") ?? "";
  if (role === "EMPLOYER") {
    const path = pathname || "/employer";
    if (!isEmployerRouteAllowed(session.accountStatus, path)) {
      redirect(getHomePath(session.role, session.accountStatus, { cin: session.cin }));
    }
  }
  if (role === "CANDIDATE") {
    const path = pathname || "/candidate";
    if (!isCandidateRouteAllowed(session.accountStatus, path)) {
      redirect(getHomePath(session.role, session.accountStatus));
    }
  }

  return <AppShell user={session}>{children}</AppShell>;
}
