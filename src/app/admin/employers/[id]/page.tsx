import { EmployerDetail } from "@/features/admin/components/employer-detail";
import { PageHeader } from "@/components/layout/page-header";

export default async function AdminEmployerDetailPage({
  params,
}: PageProps<"/admin/employers/[id]">) {
  const { id } = await params;
  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Admin", href: "/admin/dashboard" },
          { label: "Employers", href: "/admin/employers" },
          { label: "Company" },
        ]}
        title="Employer review"
        description="Approve, reject with a reason, place on hold, or release hold. Status changes need confirmation."
      />
      <EmployerDetail id={id} />
    </>
  );
}
