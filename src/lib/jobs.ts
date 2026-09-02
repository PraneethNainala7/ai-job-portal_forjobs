import type { Job } from "@/types/domain";

/** Primary must-have (required) skills, with legacy fallback to criticalSkills. */
export function getJobPrimarySkills(job: Pick<Job, "criticalSkills" | "skills">): string[] {
  if (job.skills?.length) {
    return job.skills;
  }
  if (job.criticalSkills?.length) {
    return job.criticalSkills;
  }
  return [];
}
