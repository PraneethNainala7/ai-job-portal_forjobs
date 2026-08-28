import { EmployerList } from "@/features/admin/components/employer-list";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminApprovalsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin", href: "/admin/dashboard" }, { label: "Employer approvals" }]}
        title="Employer approvals"
        description="Review company information and CIN before an employer can post jobs."
      />
      <EmployerList pendingOnly />
    </>
  );
}
