import Link from "next/link";
import { PlusIcon } from "@phosphor-icons/react/dist/ssr";
import { JobList } from "@/features/employer/components/job-list";
import { PageHeader } from "@/components/layout/page-header";
import { buttonClass } from "@/components/ui/button";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";

export default async function EmployerJobsPage() {
  await requireActiveEmployerPage();

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer", href: "/employer/dashboard" }, { label: "Manage jobs" }]}
        title="My jobs"
        description="View, edit, or close roles you posted. Closed jobs stop appearing in public search."
      />
      <div className="mb-6">
        <Link href="/employer/jobs/create" className={buttonClass()}>
          <PlusIcon size={16} weight="bold" aria-hidden />
          Create job
        </Link>
      </div>
      <JobList />
    </>
  );
}
