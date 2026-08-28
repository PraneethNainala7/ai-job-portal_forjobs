import { PageHeader } from "@/components/layout/page-header";
import { RecommendationList } from "@/features/candidate/components/recommendation-list";

export default function RecommendationsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate", href: "/candidate/dashboard" }, { label: "AI recommendations" }]}
        title="AI recommendations"
        description="Ranked suggestions based on your profile. Use them as support, not as an automatic apply."
      />
      <RecommendationList />
    </>
  );
}
