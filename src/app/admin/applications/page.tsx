import { ApplicationMonitor } from "@/features/admin/components/application-monitor";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminApplicationsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin", href: "/admin/dashboard" }, { label: "Applications" }]}
        title="Application monitoring"
        description="Platform activity only. Hiring decisions stay with employers."
      />
      <ApplicationMonitor />
    </>
  );
}
