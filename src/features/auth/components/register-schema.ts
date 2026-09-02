import { z } from "zod";

export const registerSchema = z
  .object({
    role: z.enum(["CANDIDATE", "EMPLOYER"]),
    name: z.string().trim().min(2, "Name is required."),
    email: z.string().email("Enter a valid email."),
    password: z.string().min(8, "Use at least 8 characters."),
    confirmPassword: z.string().min(1, "Confirm your password."),
    companyName: z.string().optional(),
    companyInformation: z.string().optional(),
    companyLocation: z.string().optional(),
    cin: z.string().optional(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match.",
    path: ["confirmPassword"],
  })
  .superRefine((value, context) => {
    if (value.role !== "EMPLOYER") return;
    if (!value.companyName?.trim()) {
      context.addIssue({ code: z.ZodIssueCode.custom, message: "Company name is required.", path: ["companyName"] });
    }
    if (!value.companyInformation?.trim()) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Company information is required.",
        path: ["companyInformation"],
      });
    }
    if (!value.companyLocation?.trim()) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Company location is required.",
        path: ["companyLocation"],
      });
    }
    if (!value.cin?.trim() || value.cin.trim().length < 8) {
      context.addIssue({ code: z.ZodIssueCode.custom, message: "CIN is mandatory.", path: ["cin"] });
    }
  });

export type RegisterValues = z.infer<typeof registerSchema>;
