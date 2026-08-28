"use client";

import { useQuery } from "@tanstack/react-query";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { getAdminAuditRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";

export function AuditView() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "audit"],
    queryFn: getAdminAuditRequest,
  });

  if (isLoading) return <LoadingPanel height="md" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Audit activity could not be loaded.</p>;

  const usage = [
    { label: "Resume analysis", value: data.aiUsage.resumeAnalysis },
    { label: "Job match", value: data.aiUsage.jobMatch },
    { label: "Recommendations", value: data.aiUsage.recommendations },
    { label: "Interview questions", value: data.aiUsage.interviewQuestions },
    { label: "Failed AI operations", value: data.aiUsage.failures },
  ];

  return (
    <div className="space-y-8">
      <section>
        <h2 className="mb-4 text-lg font-semibold">AI monitoring</h2>
        <p className="mb-4 text-sm text-ink-secondary">Counts only. This view does not change matching or hiring rules.</p>
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
          {usage.map((item) => (
            <article key={item.label} className="rounded-[var(--radius-card)] border bg-surface p-5">
              <p className="text-sm text-ink-secondary">{item.label}</p>
              <p className="mt-2 text-2xl font-bold">{item.value ?? 0}</p>
            </article>
          ))}
        </div>
      </section>
      <section>
        <h2 className="mb-4 text-lg font-semibold">Admin actions</h2>
        <ul className="space-y-3">
          {data.items.map((item) => (
            <li key={item.id} className="rounded-[var(--radius-card)] border bg-surface p-4 text-sm">
              <p className="font-semibold">{item.action.replaceAll("_", " ")}</p>
              <p className="mt-1 text-ink-secondary">
                {item.adminName} · {item.targetType} {item.targetId} · {formatDate(item.createdAt)}
              </p>
              {item.previousStatus || item.newStatus ? (
                <p className="mt-1 text-ink-secondary">
                  {item.previousStatus ?? "none"} to {item.newStatus ?? "none"}
                </p>
              ) : null}
              {item.reason ? <p className="mt-1 text-ink-secondary">Reason: {item.reason}</p> : null}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
