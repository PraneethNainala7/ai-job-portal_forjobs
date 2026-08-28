import { JobEditor } from "@/features/employer/components/job-editor";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";

export default async function EditJobPage({
  params,
}: PageProps<"/employer/jobs/[id]/edit">) {
  await requireActiveEmployerPage();
  const { id } = await params;

  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Employer", href: "/employer/dashboard" },
          { label: "Manage jobs", href: "/employer/jobs" },
          { label: "Edit job" },
        ]}
        title="Edit job"
        description="Updates apply to this posting only. You cannot edit another employer job from this route."
      />
      <JobEditor jobId={id} />
    </>
  );
}
