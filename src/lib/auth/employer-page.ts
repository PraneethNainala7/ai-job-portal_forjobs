import { getHomePath } from "@/config/routes";
import { getSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";

export async function requireActiveEmployerPage() {
  const session = await getSession();
  if (!session) redirect("/login");
  if (session.role !== "EMPLOYER" || session.accountStatus !== "ACTIVE") {
    redirect(getHomePath(session.role, session.accountStatus));
  }
  return session;
}

export async function requireEmployerPage() {
  const session = await getSession();
  if (!session) redirect("/login");
  if (session.role !== "EMPLOYER") redirect(getHomePath(session.role, session.accountStatus));
  return session;
}
