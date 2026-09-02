import { z } from "zod";

export const jobInputSchema = z.object({
  role: z.string().trim().min(2, "Role is required."),
  experience: z.string().trim().min(1, "Experience is required."),
  skills: z.array(z.string().trim().min(1)).optional(),
  criticalSkills: z.array(z.string().trim().min(1)).optional(),
  preferredSkills: z.array(z.string().trim().min(1)).optional(),
  educationRequirements: z.array(z.string().trim().min(1)).optional(),
  certificationRequirements: z.array(z.string().trim().min(1)).optional(),
  location: z.string().trim().min(1, "Location is required."),
  salary: z.string().trim().min(1, "Salary is required."),
  jobType: z.string().trim().min(1, "Job type is required."),
  workMode: z.string().trim().optional(),
  description: z.string().trim().min(20, "Description should be at least 20 characters."),
}).superRefine((value, ctx) => {
  const requiredCount = value.skills?.length ?? 0;
  const criticalCount = value.criticalSkills?.length ?? 0;
  if (requiredCount + criticalCount === 0) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: "Add at least one required skill.",
      path: ["skills"],
    });
  }
});
