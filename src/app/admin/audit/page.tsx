import { AuditView } from "@/features/admin/components/audit-view";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminAuditPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin", href: "/admin/dashboard" }, { label: "Audit activity" }]}
        title="Audit activity"
        description="Account and job status changes, plus AI request counts for monitoring."
      />
      <AuditView />
    </>
  );
}
