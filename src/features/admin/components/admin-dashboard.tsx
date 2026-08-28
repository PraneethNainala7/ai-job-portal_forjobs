"use client";

import {
  BriefcaseIcon,
  BuildingsIcon,
  ClipboardTextIcon,
  UsersIcon,
  WarningCircleIcon,
} from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { buttonClass } from "@/components/ui/button";
import { cardClass } from "@/components/ui/control-styles";
import { StatCard } from "@/components/ui/stat-card";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { getAdminDashboardRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";
import { cn } from "@/lib/utils";

export function AdminDashboard() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "dashboard"],
    queryFn: getAdminDashboardRequest,
  });

  if (isLoading) return <LoadingPanel height="md" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Dashboard could not be loaded.</p>;

  const cards = [
    { label: "Candidates", value: data.stats.candidates, icon: UsersIcon },
    { label: "Employers", value: data.stats.employers, icon: BuildingsIcon },
    { label: "Jobs", value: data.stats.jobs, icon: BriefcaseIcon },
    { label: "Active jobs", value: data.stats.activeJobs, icon: BriefcaseIcon },
    { label: "Applications", value: data.stats.applications, icon: ClipboardTextIcon },
    { label: "Pending employers", value: data.stats.pendingEmployers, icon: WarningCircleIcon, highlight: data.stats.pendingEmployers > 0 },
    { label: "Users", value: data.stats.users, icon: UsersIcon },
    { label: "Closed jobs", value: data.stats.closedJobs, icon: BriefcaseIcon },
  ];

  return (
    <div className="space-y-8">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <StatCard key={card.label} {...card} />
        ))}
      </div>
      {data.stats.pendingEmployers ? (
        <Link href="/admin/approvals" className={buttonClass()}>
          Review employer approvals
        </Link>
      ) : null}
      <section className="grid gap-6 lg:grid-cols-2">
        <article className={cn(cardClass, "p-5")}>
          <h2 className="font-semibold">Recent job postings</h2>
          <ul className="mt-3 space-y-3 text-sm">
            {data.recentJobs.map((job) => (
              <li key={job.id} className="flex items-start justify-between gap-3">
                <span>
                  <Link href={`/admin/jobs/${job.id}`} className="font-medium text-primary hover:text-primary-hover">
                    {job.role}
                  </Link>
                  <span className="text-ink-secondary"> · {job.companyName}</span>
                </span>
              </li>
            ))}
          </ul>
        </article>
        <article className={cn(cardClass, "p-5")}>
          <h2 className="font-semibold">Recent applications</h2>
          <ul className="mt-3 space-y-3 text-sm">
            {data.recentApplications.map((item) => (
              <li key={item.id} className="flex items-center justify-between gap-3">
                <span className="text-ink-secondary">
                  {item.candidateName} applied to {item.jobRole}
                </span>
                <StatusBadge status={item.status} />
              </li>
            ))}
          </ul>
        </article>
      </section>
      <article className={cn(cardClass, "p-5")}>
        <h2 className="font-semibold">Recent admin activity</h2>
        <ul className="mt-3 space-y-2 text-sm text-ink-secondary">
          {data.recentAudit.map((item) => (
            <li key={item.id}>
              {item.action.replaceAll("_", " ")} · {formatDate(item.createdAt)}
            </li>
          ))}
        </ul>
      </article>
    </div>
  );
}
