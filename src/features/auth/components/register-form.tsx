"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, type FormEvent } from "react";
import { useForm, useWatch } from "react-hook-form";
import { getHomePath } from "@/config/routes";
import { RegisterAccountStep } from "@/features/auth/components/register-account-step";
import { RegisterCompanyStep } from "@/features/auth/components/register-company-step";
import { registerSchema, type RegisterValues } from "@/features/auth/components/register-schema";
import { registerRequest } from "@/lib/api/auth";
import { Button } from "@/components/ui/button";

export function RegisterForm() {
  const router = useRouter();
  const params = useSearchParams();
  const initialRole = params.get("role") === "EMPLOYER" ? "EMPLOYER" : "CANDIDATE";
  const [step, setStep] = useState<1 | 2>(1);
  const [error, setError] = useState("");
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      role: initialRole,
      name: "",
      email: "",
      password: "",
      companyName: "",
      companyInformation: "",
      companyLocation: "",
      cin: "",
    },
  });
  const role = useWatch({ control: form.control, name: "role" });
  const employer = role === "EMPLOYER";

  async function onSubmit(values: RegisterValues) {
    setError("");
    try {
      const { user } = await registerRequest(values);
      router.replace(getHomePath(user.role, user.accountStatus));
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not create the account.");
    }
  }

  async function onFormSubmit(event: FormEvent<HTMLFormElement>) {
    if (employer && step === 1) {
      event.preventDefault();
      const valid = await form.trigger(["name", "email", "password"]);
      if (valid) {
        setError("");
        setStep(2);
      }
      return;
    }
    await form.handleSubmit(onSubmit)(event);
  }

  return (
    <form onSubmit={onFormSubmit} className="space-y-4">
      {employer ? (
        <div className="flex items-center gap-3">
          <span className="text-xs font-medium uppercase tracking-[0.12em] text-ink-secondary">
            Step {step} of 2
          </span>
          <span aria-hidden className="flex flex-1 gap-1.5">
            <span className="h-1 flex-1 rounded-full bg-primary" />
            <span className={step === 2 ? "h-1 flex-1 rounded-full bg-primary" : "h-1 flex-1 rounded-full bg-muted"} />
          </span>
        </div>
      ) : null}
      {step === 1 ? (
        <RegisterAccountStep
          register={form.register}
          errors={form.formState.errors}
          employer={employer}
          onRoleChange={() => setStep(1)}
        />
      ) : (
        <RegisterCompanyStep register={form.register} errors={form.formState.errors} />
      )}
      {error ? (
        <p role="alert" className="text-sm text-danger">
          {error}
        </p>
      ) : null}
      <div className="flex flex-col gap-3 pt-1 sm:flex-row-reverse">
        <Button type="submit" disabled={form.formState.isSubmitting} className="w-full">
          {form.formState.isSubmitting
            ? "Creating account..."
            : employer && step === 1
              ? "Continue"
              : "Create account"}
        </Button>
        {step === 2 ? (
          <Button type="button" variant="secondary" className="w-full sm:w-auto sm:px-6" onClick={() => setStep(1)}>
            Back
          </Button>
        ) : null}
      </div>
    </form>
  );
}
