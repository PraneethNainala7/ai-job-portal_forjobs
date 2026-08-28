import { PageHeader } from "@/components/layout/page-header";
import { ProfileForm } from "@/features/candidate/components/profile-form";

export default function CandidateProfilePage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate", href: "/candidate/dashboard" }, { label: "My profile" }]}
        title="My profile"
        description="Keep personal and professional details current so match insights stay relevant."
      />
      <ProfileForm />
    </>
  );
}
