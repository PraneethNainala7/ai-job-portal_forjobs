import { EmployerDashboard } from "@/features/employer/components/employer-dashboard";
import { getDashboardCopy } from "@/features/workspace/workspace-copy";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";

export default async function EmployerDashboardPage() {
  const session = await requireActiveEmployerPage();

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer" }, { label: "Dashboard" }]}
        title="Hiring overview"
        description={getDashboardCopy(session).description}
      />
      <EmployerDashboard />
    </>
  );
}
