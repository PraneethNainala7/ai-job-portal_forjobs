import { PageHeader } from "@/components/layout/page-header";
import { getHomePath } from "@/config/routes";
import { getFreshSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";

export default async function CandidateRestrictedPage() {
  const session = await getFreshSession();
  if (!session) redirect("/login");
  if (session.accountStatus === "ACTIVE") {
    redirect(getHomePath(session.role, session.accountStatus));
  }

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate" }, { label: "Account status" }]}
        title="Account restricted"
        description="This candidate account cannot use protected hiring features right now. Existing data is preserved."
      />
      <article className="max-w-2xl rounded-[var(--radius-card)] border bg-surface p-6">
        <p className="text-sm font-medium">Current status: {session.accountStatus.replaceAll("_", " ")}</p>
        <p className="mt-3 text-sm leading-6 text-ink-secondary">
          If you believe this is a mistake, contact the platform administrator.
        </p>
      </article>
    </>
  );
}
