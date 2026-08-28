import { formatStatus } from "@/lib/format";

const styles: Record<string, string> = {
  PENDING: "bg-pending-bg text-pending-fg",
  ACTIVE: "bg-success-bg text-success-fg",
  ON_HOLD: "bg-hold-bg text-hold-fg",
  REJECTED: "bg-rejected-bg text-rejected-fg",
  INACTIVE: "bg-muted text-ink-secondary",
  CLOSED: "bg-muted text-ink-secondary",
  APPLIED: "bg-primary-light text-primary",
  SHORTLISTED: "bg-success-bg text-success-fg",
  INTERVIEW: "bg-ai-surface text-ai",
  UNDER_REVIEW: "bg-pending-bg text-pending-fg",
  SELECTED: "bg-success-bg text-success-fg",
};

export function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${styles[status] ?? "bg-muted text-ink-secondary"}`}
    >
      <span aria-hidden className="size-1.5 rounded-full bg-current" />
      {formatStatus(status)}
    </span>
  );
}
