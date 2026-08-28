"use client";

import { useState } from "react";
import { AiPanel } from "@/components/ai/ai-panel";
import { interviewQuestionsRequest } from "@/lib/api/employer";
import type { InterviewQuestions } from "@/types/domain";

export function InterviewQuestionsPanel({
  jobId,
  candidateId,
  onGenerated,
}: {
  jobId: string;
  candidateId: string;
  onGenerated?: () => void;
}) {
  const [questions, setQuestions] = useState<InterviewQuestions | null>(null);
  const [disclaimer, setDisclaimer] = useState("");
  const [state, setState] = useState<"idle" | "loading" | "ready" | "error">("idle");

  async function generate() {
    setState("loading");
    try {
      const result = await interviewQuestionsRequest(jobId, candidateId);
      setQuestions(result.questions);
      setDisclaimer(result.disclaimer);
      setState("ready");
      onGenerated?.();
    } catch {
      setState("error");
    }
  }

  return (
    <div className="space-y-3">
      {state === "idle" ? (
        <p className="text-sm text-ink-secondary">Not generated yet. Questions are AI decision-support content.</p>
      ) : null}
      {state === "loading" ? (
        <AiPanel label="AI generated questions">
          <p className="mt-3 text-sm text-ink-secondary" aria-live="polite">Generating interview questions...</p>
        </AiPanel>
      ) : null}
      {state === "error" ? (
        <AiPanel label="AI generated questions">
          <p className="mt-3 text-sm text-danger">Question generation failed.</p>
        </AiPanel>
      ) : null}
      {state === "ready" && questions ? (
        <AiPanel label="AI generated questions">
          <p className="mt-2 text-sm text-ink-secondary">{disclaimer}</p>
          <QuestionGroup title="Technical" items={questions.technical} />
          <QuestionGroup title="Resume-based" items={questions.resumeBased} />
          <QuestionGroup title="Job-specific" items={questions.jobSpecific} />
          <QuestionGroup title="Behavioral" items={questions.behavioral} />
        </AiPanel>
      ) : null}
      <button
        type="button"
        className="h-11 w-full rounded-[var(--radius-control)] border border-ai-border bg-ai-surface text-sm font-semibold text-ai disabled:opacity-70"
        onClick={() => void generate()}
        disabled={state === "loading"}
      >
        {state === "loading" ? "Generating..." : state === "ready" ? "Regenerate AI interview questions" : "Generate AI interview questions"}
      </button>
    </div>
  );
}

function QuestionGroup({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="mt-4">
      <h4 className="text-sm font-semibold">{title}</h4>
      <ul className="mt-1 list-disc space-y-1 pl-5 text-sm text-ink-secondary">
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}
