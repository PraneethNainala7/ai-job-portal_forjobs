"use client";

import type { ResumeAnalysis } from "@/types/domain";

/** Merge every skill bucket from resume analysis, case-insensitive dedupe. */
export function allResumeSkills(resume: Pick<
  ResumeAnalysis,
  | "skills"
  | "additionalSkills"
  | "technologies"
  | "programmingLanguages"
  | "frameworks"
  | "databases"
  | "cloudTechnologies"
  | "tools"
>): string[] {
  const buckets = [
    resume.skills,
    resume.additionalSkills,
    resume.technologies,
    resume.programmingLanguages,
    resume.frameworks,
    resume.databases,
    resume.cloudTechnologies,
    resume.tools,
  ];
  const seen = new Set<string>();
  const merged: string[] = [];
  for (const bucket of buckets) {
    for (const skill of bucket ?? []) {
      const key = skill.trim().toLowerCase();
      if (!key || seen.has(key)) continue;
      seen.add(key);
      merged.push(skill.trim());
    }
  }
  return merged;
}
