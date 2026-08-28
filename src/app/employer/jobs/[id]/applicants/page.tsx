import { ApplicantTable } from "@/features/employer/components/applicant-table";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";
import { fetchEmployerJob } from "@/lib/api/server";
import { notFound } from "next/navigation";

export default async function JobApplicantsPage({
  params,
}: PageProps<"/employer/jobs/[id]/applicants">) {
  await requireActiveEmployerPage();
  const { id } = await params;
  const job = await fetchEmployerJob(id);
  if (!job) notFound();

  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Employer", href: "/employer/dashboard" },
          { label: "Applicants", href: "/employer/applicants" },
          { label: job.role },
        ]}
        title={`Applicants · ${job.role}`}
        description="Sort by match score, experience, or application date. Open a candidate to shortlist, reject, or generate interview questions."
      />
      <ApplicantTable jobId={id} />
    </>
  );
}
