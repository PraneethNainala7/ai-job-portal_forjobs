"use client";

import { ArrowRightIcon, BriefcaseIcon, ClockIcon, MapPinIcon } from "@phosphor-icons/react";
import Link from "next/link";
import { buttonClass } from "@/components/ui/button";
import { cardInteractiveClass, chipClass } from "@/components/ui/control-styles";
import { cn } from "@/lib/utils";
import type { Job } from "@/types/domain";

export function JobCard({
  job,
  href,
  insight,
  applied,
  matchHint = "sign_in",
}: {
  job: Job & { matchScore?: number };
  href?: string;
  insight?: string;
  applied?: boolean;
  matchHint?: "sign_in" | "none";
}) {
  const initial = job.companyName.trim().charAt(0).toUpperCase() || "J";
  const destination = href ?? `/jobs/${job.id}`;
  const loginNext = encodeURIComponent(destination);

  return (
    <article className={cn(cardInteractiveClass, "flex h-full flex-col p-5")}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-start gap-3">
          <span className="grid size-10 shrink-0 place-items-center rounded-[var(--radius-control)] bg-brand text-sm font-semibold text-white">
            {initial}
          </span>
          <div className="min-w-0">
            <h2 className="text-base font-semibold text-ink">{job.role}</h2>
            <p className="mt-0.5 truncate text-sm text-ink-secondary">{job.companyName}</p>
          </div>
        </div>
        <div className="flex shrink-0 flex-col items-end gap-1.5">
          {applied ? (
            <span className="rounded-full bg-primary-light px-2.5 py-1 text-xs font-semibold text-primary">
              Applied
            </span>
          ) : null}
          {typeof job.matchScore === "number" ? (
            <span className="rounded-full bg-ai-surface px-2.5 py-1 font-mono text-xs font-semibold tabular-nums text-ai">
              {job.matchScore}%
            </span>
          ) : matchHint === "sign_in" ? (
            <Link
              href={`/login?next=${loginNext}`}
              className="rounded-full bg-muted px-2.5 py-1 text-xs font-medium text-ink-secondary hover:text-primary"
            >
              Sign in for match
            </Link>
          ) : null}
        </div>
      </div>

      <ul className="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-sm text-ink-secondary">
        <Meta icon={MapPinIcon} label={job.location} />
        <Meta icon={BriefcaseIcon} label={job.jobType} />
        <Meta icon={ClockIcon} label={job.workMode ?? job.experience} />
      </ul>

      <p className="mt-3 line-clamp-2 text-sm leading-6 text-ink-secondary">{job.description}</p>

      <div className="mt-4 flex flex-wrap gap-2">
        {job.skills.slice(0, 3).map((skill) => (
          <span key={skill} className={chipClass}>
            {skill}
          </span>
        ))}
      </div>

      <p className="mt-4 text-sm font-semibold text-ink">{job.salary}</p>
      {insight ? <p className="mt-2 line-clamp-2 text-sm leading-6 text-ink-secondary">{insight}</p> : null}

      <div className="mt-auto pt-5">
        <Link href={destination} className={cn(buttonClass({ variant: "secondary", size: "sm" }), "w-full")}>
          View details
          <ArrowRightIcon size={16} aria-hidden />
        </Link>
      </div>
    </article>
  );
}

function Meta({
  icon: Icon,
  label,
}: {
  icon: typeof MapPinIcon;
  label: string;
}) {
  return (
    <li className="inline-flex items-center gap-1.5">
      <Icon size={14} className="text-ink-muted" aria-hidden />
      {label}
    </li>
  );
}
