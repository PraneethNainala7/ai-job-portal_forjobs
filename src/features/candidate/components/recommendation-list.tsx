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
  if (!data?.resumeReady) {
    return (
      <AiPanel label="AI recommendations">
        <p className="mt-3 font-semibold">Complete your resume first</p>
        <p className="mt-2 text-sm text-ink-secondary">
          Upload and analyze your resume to unlock ranked recommendations. Find jobs still lists every open role with match scores once your resume is ready.
        </p>
        <Link href="/candidate/resume" className={buttonClass({ variant: "link", className: "mt-4" })}>
          Upload resume
        </Link>
      </AiPanel>
    );
  }
  if (!data.items.length) {
    return (
      <AiPanel label="AI recommendations">
        <p className="mt-3 font-semibold">No strong matches yet</p>
        <p className="mt-2 text-sm text-ink-secondary">
          {data.evaluatedCount > 0
            ? `We reviewed ${data.evaluatedCount} open role${data.evaluatedCount === 1 ? "" : "s"} and none scored ${data.minScore}% or higher. Browse all roles in Find jobs — every card still shows its match score.`
            : "There are no open roles to rank right now. Check back later or browse Find jobs."}
        </p>
        <Link href="/candidate/jobs" className={buttonClass({ variant: "link", className: "mt-4" })}>
          Browse all jobs
        </Link>
      </AiPanel>
    );
  }

  return (
    <div className="space-y-4">
      <AiPanel label="AI recommendations">
        <p className="mt-2 text-sm text-ink-secondary">
          Curated shortlist only — roles at {data.minScore}% match or higher from{" "}
          {data.evaluatedCount} open role{data.evaluatedCount === 1 ? "" : "s"} reviewed. For every open role including lower matches, use Find jobs.
        </p>
        <p className="mt-2 text-xs text-ink-secondary">
          Showing {data.items.length} recommendation{data.items.length === 1 ? "" : "s"} above {data.minScore}% match.
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
