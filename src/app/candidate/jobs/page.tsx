import { Suspense } from "react";
import { PageHeader } from "@/components/layout/page-header";
import { LoadingInline } from "@/components/ui/loading-panel";
import { JobFilters } from "@/features/jobs/components/job-filters";
import { JobResults } from "@/features/jobs/components/job-results";

export default function CandidateJobsPage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate", href: "/candidate/dashboard" }, { label: "Find jobs" }]}
        title="Find jobs"
        description="Filter open roles. Match percentages use your current profile skills."
      />
      <Suspense fallback={<LoadingInline className="h-24" message="Loading filters..." />}>
        <JobFilters basePath="/candidate/jobs" />
      </Suspense>
      <div className="mt-8">
        <Suspense fallback={<LoadingInline className="min-h-56" message="Finding open roles..." />}>
          <JobResults basePath="/candidate/jobs" />
        </Suspense>
      </div>
    </>
  );
}
