import { PageHeader } from "@/components/layout/page-header";
import { getSession } from "@/lib/auth/session";

export default async function EmployerPendingPage() {
  const session = await getSession();
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer" }, { label: "Approval status" }]}
        title="Employer approval pending"
        description="Your company registration is being reviewed. You cannot create jobs until approval."
      />
      <article className="max-w-2xl rounded-[var(--radius-card)] border bg-surface p-6">
        <p className="text-sm font-medium">Status: Pending approval</p>
        <ul className="mt-4 space-y-2 text-sm text-ink-secondary">
          <li>Company information: {session?.companyInformation ? "Received" : "Missing"}</li>
          <li>Company location: {session?.companyLocation ?? "Not provided"}</li>
          <li>
            CIN: <span className="font-mono text-xs">{session?.cin ?? "Not provided"}</span>
          </li>
        </ul>
      </article>
    </>
  );
}
