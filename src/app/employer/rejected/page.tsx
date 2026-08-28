import { PageHeader } from "@/components/layout/page-header";
import { getSession } from "@/lib/auth/session";
import Link from "next/link";

export default async function EmployerRejectedPage() {
  const session = await getSession();
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer" }, { label: "Approval status" }]}
        title="Registration needs attention"
        description="This company registration was not approved. Update the company information and resubmit."
      />
      <article className="max-w-2xl rounded-[var(--radius-card)] border bg-surface p-6">
        <p className="text-sm font-medium text-danger">Rejected</p>
        <p className="mt-3 text-sm leading-6 text-ink-secondary">
          {session?.rejectionReason ?? "The submitted company details could not be approved."}
        </p>
        <Link
          href="/employer/profile"
          className="mt-6 inline-flex h-11 items-center rounded-[var(--radius-control)] bg-primary px-4 text-sm font-semibold text-white"
        >
          Update company and resubmit
        </Link>
      </article>
    </>
  );
}
