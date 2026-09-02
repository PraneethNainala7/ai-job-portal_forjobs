"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { RegisterCompanyStep } from "@/features/auth/components/register-company-step";
import { employerOnboardingRequest } from "@/lib/api/auth";

const schema = z.object({
  companyName: z.string().trim().min(1, "Company name is required."),
  companyInformation: z.string().trim().min(1, "Company information is required."),
  companyLocation: z.string().trim().min(1, "Company location is required."),
  cin: z.string().trim().min(8, "CIN is mandatory."),
  companyWebsite: z.string().optional(),
});

type Values = z.infer<typeof schema>;

export function EmployerOnboardingForm() {
  const router = useRouter();
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      companyName: "",
      companyInformation: "",
      companyLocation: "",
      cin: "",
      companyWebsite: "",
    },
  });

  async function onSubmit(values: Values) {
    try {
      await employerOnboardingRequest(values);
      router.replace("/employer/pending");
      router.refresh();
    } catch (err) {
      form.setError("root", {
        message: err instanceof Error ? err.message : "Could not save company details.",
      });
    }
  }

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
      <RegisterCompanyStep register={form.register} errors={form.formState.errors} />
      {form.formState.errors.root?.message ? (
        <p role="alert" className="text-sm text-danger">
          {form.formState.errors.root.message}
        </p>
      ) : null}
      <Button type="submit" disabled={form.formState.isSubmitting} className="w-full">
        {form.formState.isSubmitting ? "Submitting..." : "Submit for approval"}
      </Button>
    </form>
  );
}
