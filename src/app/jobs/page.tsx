import { Suspense } from "react";
import { PublicFooter, PublicHeader } from "@/components/layout/public-chrome";
import { LoadingInline } from "@/components/ui/loading-panel";
import { JobFilters } from "@/features/jobs/components/job-filters";
import { JobResults } from "@/features/jobs/components/job-results";

export default function JobsPage() {
  return (
    <div className="flex min-h-[100dvh] flex-col bg-canvas">
      <PublicHeader />
      <section className="mx-auto w-full max-w-[1400px] flex-1 px-4 py-8 md:px-6 lg:px-8">
        <h1 className="text-[32px] font-bold">Browse jobs</h1>
        <p className="mt-2 max-w-[60ch] text-sm leading-6 text-ink-secondary">
          Search open roles by title, skills, location, and job type. Create an account when you are ready to apply.
        </p>
        <div className="mt-6">
          <Suspense fallback={<LoadingInline className="h-24" message="Loading filters..." />}>
            <JobFilters />
          </Suspense>
        </div>
        <div className="mt-8">
          <Suspense fallback={<LoadingInline className="min-h-56" message="Finding open roles..." />}>
            <JobResults />
          </Suspense>
        </div>
      </section>
      <PublicFooter />
    </div>
  );
}
