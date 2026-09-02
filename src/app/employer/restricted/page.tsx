import { PageHeader } from "@/components/layout/page-header";
import { getHomePath } from "@/config/routes";
import { getFreshSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";

export default async function EmployerRestrictedPage() {
  const session = await getFreshSession();
  if (!session) redirect("/login");
  if (session.accountStatus === "ACTIVE") {
    redirect(getHomePath(session.role, session.accountStatus, { cin: session.cin }));
  }

  const onHold = session.accountStatus === "ON_HOLD";

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer" }, { label: "Account status" }]}
        title={onHold ? "Account on hold" : "Account restricted"}
        description={
          onHold
            ? "This employer account has restricted access. Existing jobs and applicants are preserved."
            : "This employer account cannot use hiring features right now. Existing jobs and applicants are preserved."
        }
      />
      <article className="max-w-2xl rounded-[var(--radius-card)] border bg-surface p-6">
        <p className="text-sm font-medium">Current status: {session.accountStatus.replaceAll("_", " ")}</p>
        <p className="mt-3 text-sm leading-6 text-ink-secondary">
          Contact the platform administrator if you believe this restriction was applied in error.
        </p>
      </article>
    </>
  );
}
