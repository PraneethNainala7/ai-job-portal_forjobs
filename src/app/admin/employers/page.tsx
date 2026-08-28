import { EmployerList } from "@/features/admin/components/employer-list";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminEmployersPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin", href: "/admin/dashboard" }, { label: "Employers" }]}
        title="Employers"
        description="Search companies, review status, and open an account to approve, hold, or deactivate."
      />
      <EmployerList />
    </>
  );
}
