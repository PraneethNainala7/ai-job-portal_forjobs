"use client";

import { useQuery } from "@tanstack/react-query";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { AccountActions } from "@/features/admin/components/account-actions";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { getAdminEmployerRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";

export function EmployerDetail({ id }: { id: string }) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "employer", id],
    queryFn: () => getAdminEmployerRequest(id),
  });

  if (isLoading) return <LoadingPanel height="lg" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Employer could not be loaded.</p>;

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_18rem]">
      <article className="space-y-5 rounded-[var(--radius-card)] border bg-surface p-6">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-xl font-semibold">{data.companyName ?? data.name}</h2>
          <StatusBadge status={data.accountStatus} />
        </div>
        <dl className="grid gap-4 sm:grid-cols-2 text-sm">
          <div><dt className="text-ink-muted">Recruiter</dt><dd className="mt-1">{data.name}</dd></div>
          <div><dt className="text-ink-muted">Email</dt><dd className="mt-1">{data.email}</dd></div>
          <div><dt className="text-ink-muted">Location</dt><dd className="mt-1">{data.companyLocation ?? "Not provided"}</dd></div>
          <div><dt className="text-ink-muted">Website</dt><dd className="mt-1">{data.companyWebsite ?? "Not provided"}</dd></div>
          <div><dt className="text-ink-muted">CIN</dt><dd className="mt-1 font-mono text-xs">{data.cin ?? "Not provided"}</dd></div>
          <div><dt className="text-ink-muted">Registered</dt><dd className="mt-1">{formatDate(data.createdAt)}</dd></div>
        </dl>
        <div>
          <h3 className="text-sm font-semibold">Company information</h3>
          <p className="mt-2 text-sm leading-6 text-ink-secondary">{data.companyInformation ?? "Not provided"}</p>
        </div>
        {data.rejectionReason ? (
          <p className="text-sm text-danger">Shown to employer: {data.rejectionReason}</p>
        ) : null}
        {data.holdReason ? (
          <p className="text-sm text-ink-secondary">Internal hold note (not shown to the employer): {data.holdReason}</p>
        ) : null}
      </article>
      <AccountActions id={id} kind="employer" status={data.accountStatus} />
    </div>
  );
}
