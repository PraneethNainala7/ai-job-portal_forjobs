"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { TextAreaField } from "@/components/ui/field";
import { ConfirmBar } from "@/features/admin/components/confirm-bar";
import { adminDelete, adminPut } from "@/lib/api/admin";
import type { AccountStatus } from "@/types/domain";

type Kind = "candidate" | "employer";
type Action = "approve" | "reject" | "hold" | "activate" | "deactivate" | "delete" | "resubmit";

export function AccountActions({
  id,
  kind,
  status,
}: {
  id: string;
  kind: Kind;
  status: AccountStatus;
}) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [action, setAction] = useState<Action | null>(null);
  const [reason, setReason] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ["admin"] });
    router.refresh();
  };

  const mutation = useMutation({
    mutationFn: async () => {
      if (action === "delete") return adminDelete(`/api/admin/users/${id}`);
      if (action === "approve") return adminPut(`/api/admin/employers/${id}/approve`);
      if (action === "reject") return adminPut(`/api/admin/employers/${id}/reject`, { reason });
      if (action === "resubmit") return adminPut(`/api/admin/employers/${id}/resubmit`);
      if (action === "hold") {
        const path = kind === "employer" ? `/api/admin/employers/${id}/hold` : `/api/admin/users/${id}/hold`;
        return adminPut(path, { reason });
      }
      if (action === "activate") {
        const path = kind === "employer" ? `/api/admin/employers/${id}/activate` : `/api/admin/users/${id}/activate`;
        return adminPut(path);
      }
      return adminPut(`/api/admin/users/${id}/deactivate`);
    },
    onSuccess: () => {
      if (action === "delete") {
        router.push(kind === "employer" ? "/admin/employers" : "/admin/candidates");
        return;
      }
      setMessage("Account updated.");
      setError("");
      setAction(null);
      setReason("");
      invalidate();
    },
    onError: (err: Error) => setError(err.message),
  });

  const buttons: { action: Action; label: string }[] = [];
  if (kind === "employer" && status === "PENDING") {
    buttons.push({ action: "approve", label: "Approve" }, { action: "reject", label: "Reject" });
  }
  if (kind === "employer" && status === "REJECTED") {
    buttons.push({ action: "resubmit", label: "Move to pending" }, { action: "approve", label: "Approve" });
  }
  if (status === "ACTIVE") {
    buttons.push({ action: "hold", label: "Place on hold" }, { action: "deactivate", label: "Deactivate" });
  }
  if (status === "ON_HOLD" || status === "INACTIVE") {
    buttons.push({ action: "activate", label: "Activate / release" });
  }
  buttons.push({ action: "delete", label: "Delete account" });

  return (
    <aside className="h-fit space-y-3 rounded-[var(--radius-card)] border bg-surface p-5 lg:sticky lg:top-6">
      <h2 className="text-sm font-semibold">Account actions</h2>
      {message ? <p className="text-sm" style={{ color: "#166534" }}>{message}</p> : null}
      {error ? <p className="text-sm text-danger">{error}</p> : null}
      {action ? (
        <ConfirmBar
          title={confirmTitle(action)}
          confirmLabel="Confirm"
          danger={action === "delete" || action === "reject"}
          pending={mutation.isPending}
          onConfirm={() => mutation.mutate()}
          onCancel={() => setAction(null)}
        >
          {action === "reject" || action === "hold" ? (
            <TextAreaField
              label={action === "reject" ? "Rejection reason" : "Internal hold note"}
              hint={
                action === "reject"
                  ? "Required. This text is shown to the employer."
                  : "Optional. Kept internal and not shown to the account holder."
              }
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              rows={3}
            />
          ) : null}
        </ConfirmBar>
      ) : (
        buttons.map((item) => (
          <button
            key={item.action}
            type="button"
            className={`h-11 w-full rounded-[var(--radius-control)] text-sm font-semibold ${item.action === "delete" ? "border text-danger" : "border"}`}
            onClick={() => setAction(item.action)}
          >
            {item.label}
          </button>
        ))
      )}
    </aside>
  );
}

function confirmTitle(action: Action) {
  if (action === "approve") return "Approve this employer?";
  if (action === "reject") return "Reject this registration?";
  if (action === "hold") return "Place this account on hold?";
  if (action === "activate") return "Activate this account?";
  if (action === "deactivate") return "Deactivate this account?";
  if (action === "resubmit") return "Move this employer back to pending?";
  return "Delete this account? This cannot be undone in the demo store.";
}
