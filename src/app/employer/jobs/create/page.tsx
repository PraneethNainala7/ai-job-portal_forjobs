import { JobForm } from "@/features/employer/components/job-form";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";

export default async function CreateJobPage() {
  await requireActiveEmployerPage();

  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Employer", href: "/employer/dashboard" },
          { label: "Manage jobs", href: "/employer/jobs" },
          { label: "Create job" },
        ]}
        title="Create job"
        description="Required fields: role, experience, skills, location, salary, job type, and description."
      />
      <JobForm />
    </>
  );
}
