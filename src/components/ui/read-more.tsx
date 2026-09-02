"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";

export function ReadMoreText({
  text,
  className,
  limit = 220,
}: {
  text: string;
  className?: string;
  limit?: number;
}) {
  const [expanded, setExpanded] = useState(false);
  const trimmed = text.trim();
  const needsMore = trimmed.length > limit;
  const preview = needsMore ? `${trimmed.slice(0, limit).trimEnd()}…` : trimmed;

  return (
    <div>
      <p className={className}>{expanded || !needsMore ? trimmed : preview}</p>
      {needsMore ? (
        <button
          type="button"
          onClick={() => setExpanded((value) => !value)}
          className="mt-1 text-sm font-medium text-primary hover:underline"
        >
          {expanded ? "Read less" : "Read more"}
        </button>
      ) : null}
    </div>
  );
}

export function ReadMoreList({
  items,
  initialCount = 4,
  className,
  itemClassName,
  emptyLabel,
}: {
  items: string[];
  initialCount?: number;
  className?: string;
  itemClassName?: string;
  emptyLabel: string;
}) {
  const [expanded, setExpanded] = useState(false);

  if (!items.length) {
    return <p className={cn("mt-1 text-sm text-ink-secondary", className)}>{emptyLabel}</p>;
  }

  const hiddenCount = Math.max(items.length - initialCount, 0);
  const visible = expanded ? items : items.slice(0, initialCount);

  return (
    <div className={className}>
      <ul className={cn("mt-1 space-y-1 text-sm text-ink-secondary", itemClassName)}>
        {visible.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
      {hiddenCount > 0 ? (
        <button
          type="button"
          onClick={() => setExpanded((value) => !value)}
          className="mt-2 text-sm font-medium text-primary hover:underline"
        >
          {expanded ? "Show less" : `Read more (${hiddenCount} more)`}
        </button>
      ) : null}
    </div>
  );
}
