"use client";

import { useQuery } from "@tanstack/react-query";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { JobForm } from "@/features/employer/components/job-form";
import { getEmployerJobRequest } from "@/lib/api/employer";

export function JobEditor({ jobId }: { jobId: string }) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["employer-job", jobId],
    queryFn: () => getEmployerJobRequest(jobId),
  });
  if (isLoading) return <LoadingPanel height="lg" message="Loading job..." />;
  if (isError || !data) return <p className="text-sm text-danger">Job could not be loaded.</p>;
  if (data.status === "CLOSED") return <p className="text-sm text-ink-secondary">Closed jobs cannot be edited.</p>;
  return <JobForm job={data} />;
}
