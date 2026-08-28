"use client";

import { SparkleIcon } from "@phosphor-icons/react";

export function AiPanel({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <section className="rounded-[var(--radius-card)] border border-ai-border bg-ai-surface p-5 shadow-[var(--shadow-card)]">
      <p className="inline-flex items-center gap-2 text-sm font-semibold tracking-wide text-ai">
        <SparkleIcon size={16} weight="fill" aria-hidden />
        {label}
      </p>
      {children}
    </section>
  );
}
