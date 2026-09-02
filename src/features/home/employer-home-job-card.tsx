import { ArrowRightIcon, UsersThreeIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { cardInteractiveClass } from "@/components/ui/control-styles";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { cn } from "@/lib/utils";
import type { EmployerJob } from "@/types/domain";

export function EmployerHomeJobCard({ job }: { job: EmployerJob }) {
  return (
    <Link
      href={`/employer/jobs/${job.id}`}
      className={cn(cardInteractiveClass, "flex h-full flex-col p-5 no-underline")}
    >
      <article className="flex h-full flex-col">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="text-base font-semibold text-ink">{job.role}</h3>
            <p className="mt-0.5 truncate text-sm text-ink-secondary">{job.location}</p>
          </div>
          <StatusBadge status={job.status} />
        </div>
        <p className="mt-4 inline-flex items-center gap-2 text-sm text-ink-secondary">
          <UsersThreeIcon size={16} className="text-ink-muted" aria-hidden />
          <span>
            <span className="font-mono font-medium tabular-nums text-ink">{job.applicantCount}</span> applicants
          </span>
        </p>
        <p className="mt-auto pt-5 inline-flex items-center gap-2 text-sm font-semibold text-primary">
          Manage job
          <ArrowRightIcon size={16} aria-hidden />
        </p>
      </article>
    </Link>
  );
}
