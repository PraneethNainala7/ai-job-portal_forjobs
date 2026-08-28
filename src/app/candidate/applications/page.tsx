import { PageHeader } from "@/components/layout/page-header";
import { ApplicationTable } from "@/features/candidate/components/application-list";

export default function ApplicationsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate", href: "/candidate/dashboard" }, { label: "My applications" }]}
        title="My applications"
        description="Track status after you apply. Employers decide shortlist and reject outcomes."
      />
      <ApplicationTable />
    </>
  );
}
