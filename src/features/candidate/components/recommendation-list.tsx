"use client";

import { ArrowClockwiseIcon } from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { AiPanel } from "@/components/ai/ai-panel";
import { Button, buttonClass } from "@/components/ui/button";
import { AiLoadingContent } from "@/components/ui/loading-panel";
import { JobCard } from "@/features/jobs/components/job-card";
import { getApplicationsRequest, recommendRequest } from "@/lib/api/candidate";
import { hasAppliedToJob } from "@/lib/application-status";

export function RecommendationList() {
  const { data: applications } = useQuery({
    queryKey: ["applications"],
    queryFn: getApplicationsRequest,
  });
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ["recommendations"],
    queryFn: recommendRequest,
  });

  if (isLoading) {
    return (
      <AiPanel label="AI recommendations">
        <AiLoadingContent message="Ranking roles for you..." />
      </AiPanel>
    );
  }
  if (isError) {
    return (
      <AiPanel label="AI recommendations">
        <p className="mt-3 font-semibold">Recommendations unavailable</p>
        <Button type="button" icon={ArrowClockwiseIcon} className="mt-4" onClick={() => refetch()}>
          Retry
        </Button>
      </AiPanel>
    );
  }
  if (!data?.items.length) {
    return (
      <AiPanel label="AI recommendations">
        <p className="mt-3 font-semibold">No recommendations yet</p>
        <p className="mt-2 text-sm text-ink-secondary">Add skills to your profile or upload a resume, then return here.</p>
        <Link href="/candidate/profile" className={buttonClass({ variant: "link", className: "mt-4" })}>
          Complete profile
        </Link>
      </AiPanel>
    );
  }

  return (
    <div className="space-y-4">
      <AiPanel label="AI recommendations">
        <p className="mt-2 text-sm text-ink-secondary">
          Ranked from your profile and resume analysis. Use these as support. They do not apply for you.
        </p>
      </AiPanel>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {data.items.map(({ job, match }) => (
          <JobCard
            key={job.id}
            job={{ ...job, matchScore: match.matchScore }}
            href={`/candidate/jobs/${job.id}`}
            insight={match.explanation}
            applied={hasAppliedToJob(applications?.items ?? [], job.id)}
            matchHint="none"
          />
        ))}
      </div>
    </div>
  );
}
