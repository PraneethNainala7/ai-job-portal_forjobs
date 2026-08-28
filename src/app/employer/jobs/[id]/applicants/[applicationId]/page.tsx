import { ApplicantDetail } from "@/features/employer/components/applicant-detail";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";
import { fetchEmployerJob } from "@/lib/api/server";
import { notFound } from "next/navigation";

export default async function ApplicantDetailPage({
  params,
}: PageProps<"/employer/jobs/[id]/applicants/[applicationId]">) {
  await requireActiveEmployerPage();
  const { id, applicationId } = await params;
  const job = await fetchEmployerJob(id);
  if (!job) notFound();

  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Employer", href: "/employer/dashboard" },
          { label: job.role, href: `/employer/jobs/${id}/applicants` },
          { label: "Candidate" },
        ]}
        title="Candidate review"
        description="Shortlist or reject from this screen. AI match and interview questions are supporting context only."
      />
      <ApplicantDetail jobId={id} applicationId={applicationId} />
    </>
  );
}
