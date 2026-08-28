import { EmployerDashboard } from "@/features/employer/components/employer-dashboard";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";

export default async function EmployerDashboardPage() {
  const session = await requireActiveEmployerPage();

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer" }, { label: "Dashboard" }]}
        title="Hiring overview"
        description={`${session.companyName ?? "Your company"}: track jobs, applicants, shortlists, and interviews. Match scores are decision support only.`}
      />
      <EmployerDashboard />
    </>
  );
}
