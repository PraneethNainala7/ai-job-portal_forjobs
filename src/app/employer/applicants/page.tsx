import { ApplicantsHub } from "@/features/employer/components/applicants-hub";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";

export default async function ApplicantsPage() {
  await requireActiveEmployerPage();

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer", href: "/employer/dashboard" }, { label: "Applicants" }]}
        title="Applicants"
        description="Choose a job to review candidates, match context, and hiring actions."
      />
      <ApplicantsHub />
    </>
  );
}
