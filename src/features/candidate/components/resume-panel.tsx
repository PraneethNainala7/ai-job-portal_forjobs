"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { AiPanel } from "@/components/ai/ai-panel";
import { AiLoadingContent } from "@/components/ui/loading-panel";
import { labelClass } from "@/components/ui/control-styles";
import { analyzeResumeRequest, getResumeRequest, uploadResumeRequest } from "@/lib/api/candidate";

export function ResumePanel() {
  const queryClient = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ["resume"], queryFn: getResumeRequest });
  const [error, setError] = useState("");
  const [phase, setPhase] = useState<"idle" | "uploading" | "processing">("idle");
  const handleAnalyzeResult = (result: { resume: { status: string; error?: string | null } }) => {
    if (result.resume.status === "FAILED") {
      setError(result.resume.error ?? "AI analysis failed.");
    } else {
      setError("");
    }
    void queryClient.invalidateQueries({ queryKey: ["resume"] });
    void queryClient.invalidateQueries({ queryKey: ["recommendations"] });
  };

  const mutation = useMutation({
    mutationFn: async (file: File) => {
      setError("");
      setPhase("uploading");
      await uploadResumeRequest(file);
      await queryClient.invalidateQueries({ queryKey: ["resume"] });
      setPhase("processing");
      return analyzeResumeRequest();
    },
    onSuccess: (result) => {
      setPhase("idle");
      handleAnalyzeResult(result);
    },
    onError: (err: Error) => {
      setError(err.message);
      setPhase("idle");
      void queryClient.invalidateQueries({ queryKey: ["resume"] });
    },
  });
  const reanalyze = useMutation({
    mutationFn: analyzeResumeRequest,
    onSuccess: (result) => {
      handleAnalyzeResult(result);
    },
    onError: (err: Error) => setError(err.message),
  });
  const resume = data?.resume;
  const statusText =
    phase === "uploading"
      ? "Uploading..."
      : phase === "processing" || reanalyze.isPending || resume?.status === "PROCESSING"
        ? "AI analysis in progress..."
        : null;

  return (
    <div className="space-y-6">
      <section className="rounded-[var(--radius-card)] border bg-surface p-6">
        <h2 className="text-lg font-semibold">My resume</h2>
        {isLoading ? <AiLoadingContent message="Checking resume..." /> : null}
        {!isLoading && !resume ? (
          <p className="mt-3 text-sm text-ink-secondary">No resume uploaded. Add a PDF, DOC, or DOCX file to generate an AI profile.</p>
        ) : null}
        {resume ? (
          <p className="mt-3 text-sm text-ink-secondary">
            Current file: <strong className="text-ink">{resume.fileName}</strong>
          </p>
        ) : null}
        <div className="mt-4 space-y-2">
          <label htmlFor="resume-upload" className={labelClass}>
            Upload resume
          </label>
          <input
            id="resume-upload"
            type="file"
            accept=".pdf,.doc,.docx"
            aria-describedby="resume-upload-hint"
            className="block w-full cursor-pointer rounded-[var(--radius-control)] border border-input bg-surface text-sm text-ink-secondary file:mr-3 file:h-11 file:cursor-pointer file:border-0 file:bg-muted file:px-4 file:text-sm file:font-semibold file:text-ink"
            onChange={(event) => {
              const file = event.target.files?.[0];
              if (file) mutation.mutate(file);
            }}
          />
          <p id="resume-upload-hint" className="text-xs leading-5 text-ink-secondary">
            PDF, DOC, or DOCX up to 5 MB.
          </p>
        </div>
        {statusText ? <AiLoadingContent message={statusText} /> : null}
        {error ? (
          <p role="alert" className="mt-3 text-sm text-danger">
            {error}
          </p>
        ) : null}
      </section>
      {resume?.status === "PROCESSING" && phase === "idle" ? (
        <AiPanel label="AI profile">
          <p className="mt-3 text-sm text-ink-secondary">
            Resume uploaded. Run AI analysis to extract skills and experience from your file.
          </p>
          <button
            type="button"
            className="mt-4 h-11 rounded-[var(--radius-control)] border border-ai-border px-4 text-sm font-semibold text-ai"
            onClick={() => reanalyze.mutate()}
            disabled={reanalyze.isPending}
          >
            {reanalyze.isPending ? "Analyzing..." : "Run AI analysis"}
          </button>
        </AiPanel>
      ) : null}
      {resume?.status === "FAILED" ? (
        <AiPanel label="AI profile">
          <p className="mt-3 text-sm text-danger">{resume.error ?? error ?? "AI analysis failed."}</p>
          <button
            type="button"
            className="mt-4 h-11 rounded-[var(--radius-control)] border border-ai-border px-4 text-sm font-semibold text-ai"
            onClick={() => reanalyze.mutate()}
            disabled={reanalyze.isPending}
          >
            {reanalyze.isPending ? "Analyzing..." : "Retry analysis"}
          </button>
        </AiPanel>
      ) : null}
      {resume?.status === "COMPLETE" ? (
        <AiPanel label="AI profile">
          <p className="mt-2 text-sm text-ink-secondary">Extracted from your resume as decision-support information, separate from the original file.</p>
          <div className="mt-5 grid gap-5 md:grid-cols-2">
            <List title="Primary skills" items={resume.skills} />
            <List title="Additional skills" items={resume.additionalSkills} />
            <List title="Job titles" items={resume.titles} />
            <List title="Technologies" items={resume.technologies ?? []} />
            <List title="Education" items={resume.education} />
            <List title="Certifications" items={resume.certifications} />
            <List title="Projects" items={resume.projects ?? []} />
            <List title="Industries" items={resume.industries ?? []} />
          </div>
          <p className="mt-5 text-sm text-ink-secondary">Experience: {resume.experience}. Seniority: {resume.seniority}.</p>
          <button
            type="button"
            className="mt-4 h-11 rounded-[var(--radius-control)] border border-ai-border px-4 text-sm font-semibold text-ai"
            onClick={() => reanalyze.mutate()}
            disabled={reanalyze.isPending}
          >
            {reanalyze.isPending ? "Analyzing..." : "Re-run AI analysis"}
          </button>
        </AiPanel>
      ) : null}
    </div>
  );
}

function List({ title, items }: { title: string; items: string[] }) {
  return (
    <div>
      <h3 className="text-sm font-semibold">{title}</h3>
      <ul className="mt-2 space-y-1 text-sm text-ink-secondary">
        {items.length ? items.map((item) => <li key={item}>{item}</li>) : <li>None listed</li>}
      </ul>
    </div>
  );
}
