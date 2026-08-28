"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { buttonClass } from "@/components/ui/button";
import { cardClass } from "@/components/ui/control-styles";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { getApplicationsRequest } from "@/lib/api/candidate";
import { cn } from "@/lib/utils";

export function ApplicationList() {
  const { data, isLoading, isError } = useQuery({ queryKey: ["applications"], queryFn: getApplicationsRequest });

  if (isLoading) return <LoadingPanel height="sm" message="Loading applications..." />;
  if (isError) return <p className="text-sm text-danger">Applications could not be loaded.</p>;
  if (!data?.items.length) {
    return (
      <div className={cn(cardClass, "p-6")}>
        <p className="font-semibold">No applications yet</p>
        <p className="mt-2 text-sm text-ink-secondary">Applications will appear here after you apply for jobs.</p>
        <Link href="/candidate/jobs" className={cn(buttonClass({ variant: "link" }), "mt-4")}>
          Find jobs
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {data.items.map((item) => (
        <article key={item.id} className={cn(cardClass, "p-4")}>
          <div className="flex items-start justify-between gap-3">
            <div>
              <h2 className="font-semibold">{item.job.role}</h2>
              <p className="mt-1 text-sm text-ink-secondary">{item.job.companyName}</p>
            </div>
            <StatusBadge status={item.status} />
          </div>
          <p className="mt-3 text-xs text-ink-muted">{new Date(item.appliedAt).toLocaleDateString()}</p>
        </article>
      ))}
    </div>
  );
}

export function ApplicationTable() {
  const { data, isLoading } = useQuery({ queryKey: ["applications"], queryFn: getApplicationsRequest });
  if (isLoading) return <LoadingPanel height="sm" message="Loading applications..." />;
  if (!data?.items.length) {
    return (
      <div className={cn(cardClass, "p-6")}>
        <p className="font-semibold">No applications yet</p>
        <p className="mt-2 text-sm text-ink-secondary">Applications will appear here after you apply for jobs.</p>
        <Link href="/candidate/jobs" className={cn(buttonClass({ variant: "link" }), "mt-4")}>
          Find jobs
        </Link>
      </div>
    );
  }
  return (
    <div className={cn(cardClass, "overflow-x-auto")}>
      <div className="space-y-3 p-4 md:hidden">
        {data.items.map((item) => (
          <article key={item.id} className="rounded-[var(--radius-card)] border p-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h2 className="font-semibold">{item.job.role}</h2>
                <p className="mt-1 text-sm text-ink-secondary">{item.job.companyName}</p>
              </div>
              <StatusBadge status={item.status} />
            </div>
          </article>
        ))}
      </div>
      <table className="hidden w-full min-w-[640px] text-left text-sm md:table">
        <thead className="bg-canvas text-ink-secondary">
          <tr>
            <th className="px-4 py-4 font-medium">Job</th>
            <th className="px-4 py-4 font-medium">Company</th>
            <th className="px-4 py-4 font-medium">Status</th>
            <th className="px-4 py-4 font-medium">Applied</th>
            <th className="px-4 py-4 font-medium">Details</th>
          </tr>
        </thead>
        <tbody>
          {data.items.map((item) => (
            <tr key={item.id} className="h-14 border-t hover:bg-canvas">
              <td className="px-4">{item.job.role}</td>
              <td className="px-4">{item.job.companyName}</td>
              <td className="px-4"><StatusBadge status={item.status} /></td>
              <td className="px-4">{new Date(item.appliedAt).toLocaleDateString()}</td>
              <td className="px-4">
                <Link href={`/candidate/jobs/${item.jobId}`} className="font-semibold text-primary">View</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
