import { JobModerationList } from "@/features/admin/components/job-moderation-list";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminJobsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin", href: "/admin/dashboard" }, { label: "Jobs" }]}
        title="Job moderation"
        description="Close or delete postings when needed. Do not edit employer-authored job content."
      />
      <JobModerationList />
    </>
  );
}
