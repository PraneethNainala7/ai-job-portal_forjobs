import type { Job } from "@/types/domain";

/** Primary must-have skills: critical tier, with legacy fallback to required skills. */
export function getJobPrimarySkills(job: Pick<Job, "criticalSkills" | "skills">): string[] {
  if (job.criticalSkills?.length) {
    return job.criticalSkills;
  }
  return job.skills ?? [];
}
