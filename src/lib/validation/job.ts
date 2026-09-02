import { z } from "zod";

export const jobInputSchema = z.object({
  role: z.string().trim().min(2, "Role is required."),
  experience: z.string().trim().min(1, "Experience is required."),
  skills: z.array(z.string().trim().min(1)).optional(),
  criticalSkills: z.array(z.string().trim().min(1)).min(1, "Add at least one critical skill."),
  preferredSkills: z.array(z.string().trim().min(1)).optional(),
  location: z.string().trim().min(1, "Location is required."),
  salary: z.string().trim().min(1, "Salary is required."),
  jobType: z.string().trim().min(1, "Job type is required."),
  workMode: z.string().trim().optional(),
  description: z.string().trim().min(20, "Description should be at least 20 characters."),
});
