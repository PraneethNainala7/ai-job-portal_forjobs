"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { AiPanel } from "@/components/ai/ai-panel";
import { TextAreaField } from "@/components/ui/field";
import { InterviewQuestionsPanel } from "@/features/employer/components/interview-questions-panel";
import {
  canGenerateInterviewQuestions,
  canRejectApplication,
  canShortlistApplication,
  isClosedApplication,
} from "@/lib/application-status";
import { getApplicantsRequest, rejectRequest, shortlistRequest } from "@/lib/api/employer";
import { formatStatus } from "@/lib/format";
import type { ApplicationStatus } from "@/types/domain";

export function ApplicantDetail({ jobId, applicationId }: { jobId: string; applicationId: string }) {
  const queryClient = useQueryClient();
  const { data, isLoading, isError } = useQuery({
    queryKey: ["applicants", jobId],
    queryFn: () => getApplicantsRequest(jobId),
  });
  const item = data?.items.find((entry) => entry.application.id === applicationId);
  const [confirm, setConfirm] = useState<"shortlist" | "reject" | null>(null);
  const [reason, setReason] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ["applicants", jobId] });
    void queryClient.invalidateQueries({ queryKey: ["employer-jobs"] });
    void queryClient.invalidateQueries({ queryKey: ["employer-dashboard"] });
  };

  const shortlist = useMutation({
    mutationFn: () => shortlistRequest(applicationId),
    onSuccess: () => {
      setMessage("Candidate shortlisted.");
      setError("");
      setConfirm(null);
      invalidate();
    },
    onError: (err: Error) => {
      setError(err.message);
      setConfirm(null);
    },
  });
  const reject = useMutation({
    mutationFn: () => rejectRequest(applicationId, reason),
    onSuccess: () => {
      setMessage("Candidate rejected.");
      setError("");
      setConfirm(null);
      setReason("");
      invalidate();
    },
    onError: (err: Error) => {
      setError(err.message);
      setConfirm(null);
    },
  });

  if (isLoading) return <LoadingPanel height="lg" message="Loading applicant..." />;
  if (isError || !item) return <p className="text-sm text-danger">Applicant could not be loaded.</p>;

  const status = item.application.status as ApplicationStatus;
  const canShortlist = canShortlistApplication(status);
  const canReject = canRejectApplication(status);
  const canGenerateInterview = canGenerateInterviewQuestions(status);
  const isClosed = isClosedApplication(status);

  const openConfirm = (next: "shortlist" | "reject") => {
    setError("");
    setConfirm(next);
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
      <article className="space-y-6 rounded-[var(--radius-card)] border bg-surface p-6">
        <div>
          <h2 className="text-xl font-semibold">{item.candidate.fullName}</h2>
          <p className="mt-1 text-sm text-ink-secondary">
            {item.candidate.title ?? "Candidate"} · {item.candidate.location ?? "Location not provided"} · {formatStatus(status)}
          </p>
        </div>
        <p className="text-sm text-ink-secondary">{item.candidate.experience ?? "Experience not specified"}</p>
        {item.resume?.downloadable ? (
          <p className="text-sm">
            <a
              href={`/api/employer/applications/${item.application.id}/resume`}
              className="font-semibold text-primary underline-offset-2 hover:underline"
            >
              View resume{item.resume.fileName ? ` (${item.resume.fileName})` : ""}
            </a>
          </p>
        ) : null}
        <ul className="flex flex-wrap gap-2">
          {item.candidate.skills.map((skill) => (
            <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
          ))}
        </ul>
        {item.match ? (
          <AiPanel label="AI match analysis">
            <p className="mt-2 font-mono text-3xl font-semibold tabular-nums tracking-tight">{item.match.matchScore}%</p>
            <p className="mt-2 text-sm text-ink-secondary">Use this as supporting context, not a hiring decision.</p>
            <h3 className="mt-4 text-sm font-semibold">Strengths</h3>
            <ul className="mt-1 space-y-1 text-sm text-ink-secondary">
              {item.match.strongAreas.length ? item.match.strongAreas.map((entry) => <li key={entry}>✓ {entry}</li>) : <li>None identified yet</li>}
            </ul>
            <h3 className="mt-4 text-sm font-semibold">Skill gaps</h3>
            <ul className="mt-1 space-y-1 text-sm text-ink-secondary">
              {item.match.gaps.length ? item.match.gaps.map((entry) => <li key={entry}>○ {entry}</li>) : <li>No required-skill gaps</li>}
            </ul>
            {item.match.explanation ? <p className="mt-4 text-sm leading-6 text-ink-secondary">{item.match.explanation}</p> : null}
          </AiPanel>
        ) : (
          <p className="text-sm text-ink-secondary">Match score is not available for this application.</p>
        )}
      </article>
      <aside className="h-fit space-y-3 rounded-[var(--radius-card)] border bg-surface p-5 lg:sticky lg:top-6">
        {message ? <p className="text-sm" style={{ color: "#166534" }}>{message}</p> : null}
        {error ? <p className="text-sm text-danger">{error}</p> : null}
        {confirm === "shortlist" ? (
          <div className="space-y-2">
            <p className="text-sm">Shortlist this candidate?</p>
            <button className="h-11 w-full rounded-[var(--radius-control)] bg-primary text-sm font-semibold text-white" onClick={() => shortlist.mutate()}>
              Confirm shortlist
            </button>
            <button className="h-11 w-full rounded-[var(--radius-control)] border text-sm font-semibold" onClick={() => setConfirm(null)}>Cancel</button>
          </div>
        ) : confirm === "reject" ? (
          <div className="space-y-2">
            <p className="text-sm">Reject this candidate?</p>
            <TextAreaField
              label="Rejection reason"
              hint="Optional. Shared with the candidate."
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              rows={3}
            />
            <button className="h-11 w-full rounded-[var(--radius-control)] bg-danger text-sm font-semibold text-white" onClick={() => reject.mutate()}>
              Confirm reject
            </button>
            <button className="h-11 w-full rounded-[var(--radius-control)] border text-sm font-semibold" onClick={() => setConfirm(null)}>Cancel</button>
          </div>
        ) : isClosed ? (
          <p className="text-sm text-ink-secondary">This application is closed.</p>
        ) : (
          <>
            {canShortlist ? (
              <button className="h-11 w-full rounded-[var(--radius-control)] bg-primary text-sm font-semibold text-white" onClick={() => openConfirm("shortlist")}>
                Shortlist
              </button>
            ) : null}
            {canReject ? (
              <button className="h-11 w-full rounded-[var(--radius-control)] border text-sm font-semibold" onClick={() => openConfirm("reject")}>
                Reject
              </button>
            ) : null}
            {canGenerateInterview ? (
              <InterviewQuestionsPanel jobId={jobId} candidateId={item.candidate.id} onGenerated={invalidate} />
            ) : null}
          </>
        )}
      </aside>
    </div>
  );
}
