import { PageHeader } from "@/components/layout/page-header";
import { getSession } from "@/lib/auth/session";

export default async function EmployerRestrictedPage() {
  const session = await getSession();
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer" }, { label: "Account status" }]}
        title="Account on hold"
        description="This employer account has restricted access. Existing jobs and applicants are preserved."
      />
      <article className="max-w-2xl rounded-[var(--radius-card)] border bg-surface p-6">
        <p className="text-sm font-medium">Current status: {session?.accountStatus.replaceAll("_", " ")}</p>
      </article>
    </>
  );
}
