import { AdminDashboard } from "@/features/admin/components/admin-dashboard";
import { getDashboardCopy } from "@/features/workspace/workspace-copy";
import { PageHeader } from "@/components/layout/page-header";
import { getSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";

export default async function AdminDashboardPage() {
  const session = await getSession();
  if (!session || session.role !== "ADMIN") redirect("/login");

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin" }, { label: "Dashboard" }]}
        title="Platform overview"
        description={getDashboardCopy(session).description}
      />
      <AdminDashboard />
    </>
  );
}
