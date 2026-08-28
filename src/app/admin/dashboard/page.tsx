import { AdminDashboard } from "@/features/admin/components/admin-dashboard";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminDashboardPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin" }, { label: "Dashboard" }]}
        title="Platform overview"
        description="Monitor users, jobs, and applications. Pending employer approvals are the primary action."
      />
      <AdminDashboard />
    </>
  );
}
