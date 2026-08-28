"use client";

import { ArrowClockwiseIcon, MagnifyingGlassIcon } from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { Button, buttonClass } from "@/components/ui/button";
import { cardClass } from "@/components/ui/control-styles";
import { JobCard } from "@/features/jobs/components/job-card";
import { getApplicationsRequest } from "@/lib/api/candidate";
import { fetchJobs } from "@/lib/api/jobs";
import { hasAppliedToJob } from "@/lib/application-status";
import { cn } from "@/lib/utils";

export function JobResults({ basePath = "/jobs" }: { basePath?: string }) {
  const params = useSearchParams();
  const query = Object.fromEntries(params.entries());
  const page = Number(query.page ?? 1);
  const isCandidateBrowse = basePath === "/candidate/jobs";
  const { data: applications } = useQuery({
    queryKey: ["applications"],
    queryFn: getApplicationsRequest,
    enabled: isCandidateBrowse,
  });
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ["jobs", basePath, query],
    queryFn: () => fetchJobs(query),
  });

  if (isLoading) {
    return <LoadingPanel height="md" message="Finding open roles..." />;
  }

  if (isError) {
    return (
      <div className={cn(cardClass, "p-6")}>
        <p className="font-semibold">Jobs could not be loaded</p>
        <p className="mt-2 text-sm text-ink-secondary">Check your connection and try again.</p>
        <Button type="button" icon={ArrowClockwiseIcon} className="mt-4" onClick={() => refetch()}>
          Retry
        </Button>
      </div>
    );
  }

  if (!data?.items.length) {
    return (
      <div className={cn(cardClass, "p-6")}>
        <p className="font-semibold">No jobs found</p>
        <p className="mt-2 text-sm text-ink-secondary">Try a broader role, skill, or location, or reset filters.</p>
      </div>
    );
  }

  const totalPages = Math.ceil(data.total / data.pageSize);

  return (
    <div>
      <p className="mb-4 inline-flex items-center gap-2 text-sm text-ink-secondary">
        <MagnifyingGlassIcon size={16} aria-hidden />
        <span className="font-mono tabular-nums font-medium text-ink">{data.total}</span> open roles
      </p>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {data.items.map((job) => (
          <JobCard
            key={job.id}
            job={job}
            href={`${basePath}/${job.id}`}
            applied={isCandidateBrowse && hasAppliedToJob(applications?.items ?? [], job.id)}
            matchHint={isCandidateBrowse ? "none" : "sign_in"}
          />
        ))}
      </div>
      {totalPages > 1 ? (
        <div className="mt-6 flex flex-wrap gap-2">
          {Array.from({ length: totalPages }, (_, index) => {
            const next = new URLSearchParams(params.toString());
            next.set("page", String(index + 1));
            const active = page === index + 1;
            return (
              <Link
                key={index}
                href={`${basePath}?${next.toString()}`}
                className={buttonClass({
                  variant: active ? "primary" : "secondary",
                  size: "sm",
                  className: "min-w-11 px-3",
                })}
              >
                {index + 1}
              </Link>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
