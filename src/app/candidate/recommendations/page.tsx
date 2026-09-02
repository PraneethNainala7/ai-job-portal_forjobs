import { PageHeader } from "@/components/layout/page-header";
import { RecommendationList } from "@/features/candidate/components/recommendation-list";

export default function RecommendationsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate", href: "/candidate/dashboard" }, { label: "AI recommendations" }]}
        title="AI recommendations"
        description="Curated roles scoring 60% or higher against your resume. Lower matches stay visible in Find jobs with their scores."
      />
      <RecommendationList />
    </>
  );
}
