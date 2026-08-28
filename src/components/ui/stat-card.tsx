import type { Icon } from "@phosphor-icons/react";
import { cn } from "@/lib/utils";

export function StatCard({
  label,
  value,
  icon: Icon,
  highlight = false,
}: {
  label: string;
  value: string | number;
  icon: Icon;
  highlight?: boolean;
}) {
  return (
    <article
      className={cn(
        "rounded-[var(--radius-card)] border bg-surface p-5 shadow-[var(--shadow-card)]",
        highlight && "border-primary/25 bg-primary-light",
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm text-ink-secondary">{label}</p>
        <span
          className={cn(
            "grid size-9 place-items-center rounded-[var(--radius-control)]",
            highlight ? "bg-primary text-white" : "bg-muted text-ink-secondary",
          )}
        >
          <Icon size={18} aria-hidden />
        </span>
      </div>
      <p className="mt-3 font-mono text-3xl font-semibold tabular-nums tracking-tight">{value}</p>
    </article>
  );
}
