import { JobModerationDetail } from "@/features/admin/components/job-moderation-detail";
import { PageHeader } from "@/components/layout/page-header";

export default async function AdminJobDetailPage({
  params,
}: PageProps<"/admin/jobs/[id]">) {
  const { id } = await params;
  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Admin", href: "/admin/dashboard" },
          { label: "Jobs", href: "/admin/jobs" },
          { label: "Job" },
        ]}
        title="Job details"
        description="Read-only posting. Close it for moderation or delete it if the business rule allows."
      />
      <JobModerationDetail id={id} />
    </>
  );
}
