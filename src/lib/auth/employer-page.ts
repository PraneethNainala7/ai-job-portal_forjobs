import { getHomePath } from "@/config/routes";
import { getFreshSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";

export async function requireActiveEmployerPage() {
  const session = await getFreshSession();
  if (!session) redirect("/login");
  if (session.role !== "EMPLOYER" || session.accountStatus !== "ACTIVE") {
    redirect(getHomePath(session.role, session.accountStatus, { cin: session.cin }));
  }
  return session;
}

export async function requireEmployerPage() {
  const session = await getFreshSession();
  if (!session) redirect("/login");
  if (session.role !== "EMPLOYER") redirect(getHomePath(session.role, session.accountStatus, { cin: session.cin }));
  return session;
}
