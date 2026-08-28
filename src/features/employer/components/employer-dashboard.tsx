"use client";

import {
  BriefcaseIcon,
  CalendarCheckIcon,
  PlusIcon,
  UserCircleIcon,
  UsersThreeIcon,
} from "@phosphor-icons/react";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { cardInteractiveClass } from "@/components/ui/control-styles";
import { StatCard } from "@/components/ui/stat-card";
import { getEmployerDashboardRequest } from "@/lib/api/employer";
import { cn } from "@/lib/utils";

const actions = [
  { href: "/employer/jobs", label: "View my jobs", icon: BriefcaseIcon },
  { href: "/employer/jobs/create", label: "Create job", icon: PlusIcon },
  { href: "/employer/applicants", label: "View applicants", icon: UsersThreeIcon },
  { href: "/employer/profile", label: "Manage profile", icon: UserCircleIcon },
];

export function EmployerDashboard() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["employer-dashboard"],
    queryFn: getEmployerDashboardRequest,
  });

  if (isLoading) return <LoadingPanel height="sm" message="Loading dashboard..." />;
  if (isError || !data) return <p className="text-sm text-danger">Dashboard stats could not be loaded.</p>;

  const stats = [
    { label: "Jobs", value: data.jobs, icon: BriefcaseIcon },
    { label: "Applicants", value: data.applicants, icon: UsersThreeIcon, highlight: data.applicants > 0 },
    { label: "Shortlisted", value: data.shortlisted, icon: UserCircleIcon },
    { label: "Interviews", value: data.interviews, icon: CalendarCheckIcon },
  ];

  return (
    <>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((stat) => (
          <StatCard key={stat.label} {...stat} />
        ))}
      </div>
      <div className="mt-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {actions.map((action) => {
          const Icon = action.icon;
          return (
            <Link key={action.href} href={action.href} className={cn(cardInteractiveClass, "flex items-center gap-3 p-5 text-sm font-semibold")}>
              <span className="grid size-9 place-items-center rounded-[var(--radius-control)] bg-muted text-ink-secondary">
                <Icon size={18} aria-hidden />
              </span>
              {action.label}
            </Link>
          );
        })}
      </div>
    </>
  );
}
