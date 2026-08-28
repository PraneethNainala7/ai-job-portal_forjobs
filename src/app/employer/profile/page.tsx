import { CompanyProfileForm } from "@/features/employer/components/company-profile-form";
import { PageHeader } from "@/components/layout/page-header";
import { getHomePath } from "@/config/routes";
import { requireEmployerPage } from "@/lib/auth/employer-page";

export default async function EmployerProfilePage() {
  const session = await requireEmployerPage();
  const canResubmit = session.accountStatus === "REJECTED";

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Employer", href: getHomePath(session.role, session.accountStatus) }, { label: "Company profile" }]}
        title="Company profile"
        description="Keep company information current. Pending and rejected accounts can still update these details."
      />
      <CompanyProfileForm canResubmit={canResubmit} />
    </>
  );
}
