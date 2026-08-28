"use client";

import { Button } from "@/components/ui/button";

export function ConfirmBar({
  title,
  confirmLabel,
  onConfirm,
  onCancel,
  pending,
  danger,
  children,
}: {
  title: string;
  confirmLabel: string;
  onConfirm: () => void;
  onCancel: () => void;
  pending?: boolean;
  danger?: boolean;
  children?: React.ReactNode;
}) {
  return (
    <div className="space-y-3 rounded-[var(--radius-card)] border bg-muted p-4">
      <p className="text-sm font-medium">{title}</p>
      {children}
      <div className="flex flex-col gap-2 sm:flex-row">
        <Button type="button" disabled={pending} onClick={onConfirm} variant={danger ? "danger" : "primary"}>
          {pending ? "Working..." : confirmLabel}
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
      </div>
    </div>
  );
}
