"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { getEmployerJobsRequest } from "@/lib/api/employer";

export function ApplicantsHub() {
  const { data, isLoading, isError } = useQuery({ queryKey: ["employer-jobs"], queryFn: getEmployerJobsRequest });

  if (isLoading) return <LoadingPanel height="md" message="Loading jobs..." />;
  if (isError || !data) return <p className="text-sm text-danger">Jobs could not be loaded.</p>;
  if (!data.items.length) {
    return <p className="rounded-[var(--radius-card)] border bg-surface p-6 text-sm text-ink-secondary">Create a job to start receiving applicants.</p>;
  }

  return (
    <ul className="space-y-4">
      {data.items.map((job) => (
        <li key={job.id} className="flex flex-col gap-3 rounded-[var(--radius-card)] border bg-surface p-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="font-semibold">{job.role}</h2>
            <p className="mt-1 text-sm text-ink-secondary">
              {job.applicantCount} applicants · {job.shortlistedCount} shortlisted or in interview
            </p>
          </div>
          <Link href={`/employer/jobs/${job.id}/applicants`} className="inline-flex h-10 items-center justify-center rounded-[var(--radius-control)] bg-primary px-4 text-sm font-semibold text-white">
            Review applicants
          </Link>
        </li>
      ))}
    </ul>
  );
}
