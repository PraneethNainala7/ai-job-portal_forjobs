import { CandidateList } from "@/features/admin/components/candidate-list";
import { PageHeader } from "@/components/layout/page-header";

export default function AdminCandidatesPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Admin", href: "/admin/dashboard" }, { label: "Candidates" }]}
        title="Candidates"
        description="Search and filter accounts. Resume file contents are not shown in list views."
      />
      <CandidateList />
    </>
  );
}
