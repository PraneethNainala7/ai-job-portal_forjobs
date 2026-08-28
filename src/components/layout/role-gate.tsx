import { AppShell } from "@/components/layout/app-shell";
import { getHomePath } from "@/config/routes";
import { getSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";
import type { UserRole } from "@/types/domain";

export async function RoleGate({
  role,
  children,
}: {
  role: UserRole;
  children: React.ReactNode;
}) {
  const session = await getSession();
  if (!session) redirect("/login");
  if (session.role !== role) redirect(getHomePath(session.role, session.accountStatus));
  return <AppShell user={session}>{children}</AppShell>;
}
