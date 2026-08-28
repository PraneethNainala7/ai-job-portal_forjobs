"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { ConfirmBar } from "@/features/admin/components/confirm-bar";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { adminDelete, adminPut, getAdminJobRequest } from "@/lib/api/admin";

export function JobModerationDetail({ id }: { id: string }) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "job", id],
    queryFn: () => getAdminJobRequest(id),
  });
  const [action, setAction] = useState<"close" | "delete" | null>(null);
  const [error, setError] = useState("");
  const mutation = useMutation({
    mutationFn: () => (action === "delete" ? adminDelete(`/api/admin/jobs/${id}`) : adminPut(`/api/admin/jobs/${id}/close`)),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["admin"] });
      if (action === "delete") router.push("/admin/jobs");
      else setAction(null);
    },
    onError: (err: Error) => setError(err.message),
  });

  if (isLoading) return <LoadingPanel height="lg" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Job could not be loaded.</p>;

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_18rem]">
      <article className="space-y-5 rounded-[var(--radius-card)] border bg-surface p-6">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-xl font-semibold">{data.role}</h2>
          <StatusBadge status={data.status} />
        </div>
        <p className="text-sm text-ink-secondary">
          {data.companyName} · {data.employerName ?? "Employer not linked"} · {data.location} · {data.jobType}
        </p>
        <p className="text-sm text-ink-secondary">Applicants: {data.applicantCount} · Posted {data.postedDate}</p>
        <p className="text-sm leading-7 text-ink-secondary">{data.description}</p>
        <p className="text-sm text-ink-muted">Job content is read-only. Employers edit their own postings.</p>
      </article>
      <aside className="h-fit space-y-3 rounded-[var(--radius-card)] border bg-surface p-5">
        {error ? <p className="text-sm text-danger">{error}</p> : null}
        {action ? (
          <ConfirmBar
            title={action === "delete" ? "Delete this job?" : "Close this job for moderation?"}
            confirmLabel="Confirm"
            danger={action === "delete"}
            pending={mutation.isPending}
            onConfirm={() => mutation.mutate()}
            onCancel={() => setAction(null)}
          />
        ) : (
          <>
            {data.status === "ACTIVE" ? (
              <button type="button" className="h-11 w-full rounded-[var(--radius-control)] border text-sm font-semibold" onClick={() => setAction("close")}>
                Close job
              </button>
            ) : null}
            <button type="button" className="h-11 w-full rounded-[var(--radius-control)] border text-sm font-semibold text-danger" onClick={() => setAction("delete")}>
              Delete job
            </button>
          </>
        )}
      </aside>
    </div>
  );
}
