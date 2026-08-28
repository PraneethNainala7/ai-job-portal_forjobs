import { CandidateDetail } from "@/features/admin/components/candidate-detail";
import { PageHeader } from "@/components/layout/page-header";

export default async function AdminCandidateDetailPage({
  params,
}: PageProps<"/admin/candidates/[id]">) {
  const { id } = await params;
  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Admin", href: "/admin/dashboard" },
          { label: "Candidates", href: "/admin/candidates" },
          { label: "Profile" },
        ]}
        title="Candidate account"
        description="Activate, hold, or deactivate this account. Profile history stays preserved."
      />
      <CandidateDetail id={id} />
    </>
  );
}
