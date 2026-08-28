"use client";

import { useQuery } from "@tanstack/react-query";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { AccountActions } from "@/features/admin/components/account-actions";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { getAdminCandidateRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";

export function CandidateDetail({ id }: { id: string }) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "candidate", id],
    queryFn: () => getAdminCandidateRequest(id),
  });

  if (isLoading) return <LoadingPanel height="lg" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Candidate could not be loaded.</p>;

  const { account, profile, resumeSummary } = data;

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_18rem]">
      <article className="space-y-6 rounded-[var(--radius-card)] border bg-surface p-6">
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-xl font-semibold">{account.name}</h2>
          <StatusBadge status={account.accountStatus} />
        </div>
        <dl className="grid gap-4 sm:grid-cols-2 text-sm">
          <div><dt className="text-ink-muted">Email</dt><dd className="mt-1">{account.email}</dd></div>
          <div><dt className="text-ink-muted">Registered</dt><dd className="mt-1">{formatDate(account.createdAt)}</dd></div>
          <div><dt className="text-ink-muted">Title</dt><dd className="mt-1">{profile.title ?? "Not specified"}</dd></div>
          <div><dt className="text-ink-muted">Location</dt><dd className="mt-1">{profile.location ?? "Not specified"}</dd></div>
          <div><dt className="text-ink-muted">Experience</dt><dd className="mt-1">{profile.experience ?? "Not specified"}</dd></div>
        </dl>
        <div>
          <h3 className="text-sm font-semibold">Skills</h3>
          <ul className="mt-2 flex flex-wrap gap-2">
            {profile.skills.length ? profile.skills.map((skill) => (
              <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
            )) : <li className="text-sm text-ink-secondary">No skills listed</li>}
          </ul>
        </div>
        {resumeSummary ? (
          <p className="text-sm text-ink-secondary">
            Resume on file: {resumeSummary.fileName} ({formatDate(resumeSummary.uploadedAt)}). File contents are not shown in this list-style admin view.
          </p>
        ) : (
          <p className="text-sm text-ink-secondary">No resume uploaded.</p>
        )}
      </article>
      <AccountActions id={id} kind="candidate" status={account.accountStatus} />
    </div>
  );
}
