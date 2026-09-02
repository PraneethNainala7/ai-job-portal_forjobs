"use client";

import { useQuery } from "@tanstack/react-query";
import { AiPanel } from "@/components/ai/ai-panel";
import { AiLoadingContent } from "@/components/ui/loading-panel";
import { MatchBreakdownPanel } from "@/features/candidate/components/match-breakdown-panel";
import { matchRequest } from "@/lib/api/candidate";

export function MatchPanel({ jobId }: { jobId: string }) {
  const { data, isLoading, isError, refetch, isFetching } = useQuery({
    queryKey: ["match", jobId],
    queryFn: () => matchRequest(jobId),
  });

  if (isLoading) {
    return (
      <AiPanel label="AI match analysis">
        <AiLoadingContent message="Analyzing your match..." />
      </AiPanel>
    );
  }

  if (isError || !data) {
    return (
      <AiPanel label="AI match analysis">
        <p className="mt-3 text-sm text-ink-secondary">Match analysis is unavailable for this role.</p>
        <button type="button" onClick={() => refetch()} className="mt-4 h-11 rounded-[var(--radius-control)] border border-ai-border px-4 text-sm font-semibold text-ai">
          Retry
        </button>
      </AiPanel>
    );
  }

  return (
    <AiPanel label="AI match analysis">
      <p className="mt-2 font-mono text-3xl font-semibold tabular-nums tracking-tight">{data.matchScore}%</p>
      <p className="mt-2 text-sm text-ink-secondary">Decision support only. This is not a hiring decision.</p>
      <MatchBreakdownPanel match={data} />
      {isFetching ? <AiLoadingContent message="Refreshing..." className="mt-3" /> : null}
    </AiPanel>
  );
}
