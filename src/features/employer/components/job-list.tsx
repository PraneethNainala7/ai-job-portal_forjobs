"use client";

import { PencilSimpleIcon, UsersThreeIcon, EyeIcon } from "@phosphor-icons/react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { Button, buttonClass } from "@/components/ui/button";
import { cardClass } from "@/components/ui/control-styles";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { closeEmployerJobRequest, getEmployerJobsRequest } from "@/lib/api/employer";
import { cn } from "@/lib/utils";

export function JobList() {
  const queryClient = useQueryClient();
  const { data, isLoading, isError } = useQuery({ queryKey: ["employer-jobs"], queryFn: getEmployerJobsRequest });
  const [pendingId, setPendingId] = useState<string | null>(null);
  const close = useMutation({
    mutationFn: closeEmployerJobRequest,
    onSuccess: () => {
      setPendingId(null);
      void queryClient.invalidateQueries({ queryKey: ["employer-jobs"] });
      void queryClient.invalidateQueries({ queryKey: ["employer-dashboard"] });
    },
  });

  if (isLoading) return <LoadingPanel height="md" message="Loading jobs..." />;
  if (isError || !data) return <p className="text-sm text-danger">Jobs could not be loaded.</p>;
  if (!data.items.length) {
    return (
      <div className={cn(cardClass, "p-8 text-sm text-ink-secondary")}>
        No jobs yet.{" "}
        <Link href="/employer/jobs/create" className="font-semibold text-primary">
          Create a job
        </Link>
      </div>
    );
  }

  return (
    <ul className="space-y-4">
      {data.items.map((job) => (
        <li key={job.id} className={cn(cardClass, "p-5")}>
          <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-lg font-semibold">{job.role}</h2>
                <StatusBadge status={job.status} />
              </div>
              <p className="mt-2 text-sm text-ink-secondary">
                <span className="font-mono font-medium tabular-nums text-ink">{job.applicantCount}</span> applicants
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Link href={`/employer/jobs/${job.id}`} className={buttonClass({ variant: "secondary", size: "sm" })}>
                <EyeIcon size={16} aria-hidden />
                View
              </Link>
              {job.status === "ACTIVE" ? (
                <Link href={`/employer/jobs/${job.id}/edit`} className={buttonClass({ variant: "secondary", size: "sm" })}>
                  <PencilSimpleIcon size={16} aria-hidden />
                  Edit
                </Link>
              ) : null}
              <Link href={`/employer/jobs/${job.id}/applicants`} className={buttonClass({ variant: "secondary", size: "sm" })}>
                <UsersThreeIcon size={16} aria-hidden />
                Applicants
              </Link>
              {job.status === "ACTIVE" ? (
                pendingId === job.id ? (
                  <span className="flex gap-2">
                    <Button size="sm" variant="danger" onClick={() => close.mutate(job.id)}>
                      Confirm close
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => setPendingId(null)}>
                      Cancel
                    </Button>
                  </span>
                ) : (
                  <Button size="sm" variant="secondary" onClick={() => setPendingId(job.id)}>
                    Close
                  </Button>
                )
              ) : null}
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
