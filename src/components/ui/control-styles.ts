export const labelClass = "block text-sm font-medium text-ink";

export const controlClass =
  "h-11 w-full rounded-[var(--radius-control)] border border-input bg-surface px-3 text-sm font-normal text-ink placeholder:text-ink-muted";

export const textAreaClass =
  "w-full rounded-[var(--radius-control)] border border-input bg-surface px-3 py-2 text-sm font-normal leading-6 text-ink placeholder:text-ink-muted";

export const cardClass =
  "rounded-[var(--radius-card)] border bg-surface shadow-[var(--shadow-card)]";

export const cardInteractiveClass =
  `${cardClass} transition-[border-color,box-shadow] duration-200 hover:border-input hover:shadow-[var(--shadow-card-hover)]`;

export const chipClass =
  "inline-flex items-center rounded-full bg-muted px-2.5 py-1 text-xs font-medium text-ink-secondary";

export const filterPanelClass =
  "rounded-[var(--radius-card)] border bg-surface p-4 shadow-[var(--shadow-card)] md:p-5";
