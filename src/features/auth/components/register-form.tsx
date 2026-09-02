"use client";



import { zodResolver } from "@hookform/resolvers/zod";

import { useRouter, useSearchParams } from "next/navigation";

import { useEffect, useState, type FormEvent } from "react";

import { useForm, useWatch } from "react-hook-form";

import { getHomePath } from "@/config/routes";

import { RegisterAccountStep } from "@/features/auth/components/register-account-step";

import { RegisterCompanyStep } from "@/features/auth/components/register-company-step";

import { RegisterRolePicker } from "@/features/auth/components/register-role-picker";

import { GoogleSignInButton } from "@/features/auth/components/google-sign-in-button";

import { registerSchema, type RegisterValues } from "@/features/auth/components/register-schema";

import type { RegisterRole } from "@/features/auth/register-copy";

import { employerOnboardingRequest, registerRequest, sessionRequest } from "@/lib/api/auth";

import { Button } from "@/components/ui/button";

import { LoadingPanel } from "@/components/ui/loading-panel";

function createDefaultValues(role: RegisterRole): RegisterValues {
  return {
    role,
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    companyName: "",
    companyInformation: "",
    companyLocation: "",
    cin: "",
  };
}



export function RegisterForm({

  displayRole,

  onRoleChange,

}: {

  displayRole: RegisterRole;

  onRoleChange: (role: RegisterRole) => void;

}) {

  const router = useRouter();

  const params = useSearchParams();

  const urlStep = params.get("step") === "2" ? 2 : 1;

  const [step, setStep] = useState<1 | 2>(urlStep);

  const [employerOnboarding, setEmployerOnboarding] = useState(false);

  const [sessionCheckPending, setSessionCheckPending] = useState(urlStep === 2);

  const [signedInEmail, setSignedInEmail] = useState("");

  const [error, setError] = useState("");

  const form = useForm<RegisterValues>({

    resolver: zodResolver(registerSchema),

    defaultValues: createDefaultValues(displayRole),

  });

  const role = useWatch({ control: form.control, name: "role" });

  const employer = role === "EMPLOYER";



  function resetFormForRole(role: RegisterRole) {
    setStep(1);
    setError("");
    form.reset(createDefaultValues(role));
  }

  function handleRoleChange() {
    const newRole = form.getValues("role");
    resetFormForRole(newRole);
    onRoleChange(newRole);
  }



  useEffect(() => {

    if (urlStep === 2 || employerOnboarding || step === 2) return;

    if (form.getValues("role") !== displayRole) {
      resetFormForRole(displayRole);
    }

  }, [displayRole, urlStep, employerOnboarding, step, form]);



  useEffect(() => {

    if (urlStep !== 2) {

      setSessionCheckPending(false);

      return;

    }



    let cancelled = false;

    void sessionRequest()

      .then(({ user }) => {

        if (cancelled) return;

        if (user?.role === "EMPLOYER" && !user.cin) {

          setEmployerOnboarding(true);

          setSignedInEmail(user.email);

          setStep(2);

          form.setValue("role", "EMPLOYER");

          onRoleChange("EMPLOYER");

          return;

        }

        if (!user) {

          router.replace("/register?role=EMPLOYER");

          return;

        }

        router.replace(getHomePath(user.role, user.accountStatus, { cin: user.cin }));

      })

      .catch(() => {

        if (!cancelled) router.replace("/register?role=EMPLOYER");

      })

      .finally(() => {

        if (!cancelled) setSessionCheckPending(false);

      });



    return () => {

      cancelled = true;

    };

  }, [form, onRoleChange, router, urlStep]);



  async function onSubmit(values: RegisterValues) {

    setError("");

    try {

      if (employerOnboarding) {

        await employerOnboardingRequest({

          companyName: values.companyName ?? "",

          companyInformation: values.companyInformation ?? "",

          companyLocation: values.companyLocation ?? "",

          cin: values.cin ?? "",

        });

        router.replace("/employer/pending");

        router.refresh();

        return;

      }

      const { confirmPassword: _, ...payload } = values;

      const { user } = await registerRequest(payload);

      router.replace(getHomePath(user.role, user.accountStatus, { cin: user.cin }));

      router.refresh();

    } catch (err) {

      setError(err instanceof Error ? err.message : "Could not create the account.");

    }

  }



  async function onFormSubmit(event: FormEvent<HTMLFormElement>) {

    if (employerOnboarding) {

      event.preventDefault();

      const valid = await form.trigger(["companyName", "companyInformation", "companyLocation", "cin"]);

      if (valid) {

        await onSubmit(form.getValues());

      }

      return;

    }

    if (employer && step === 1) {

      event.preventDefault();

      const valid = await form.trigger(["name", "email", "password", "confirmPassword"]);

      if (valid) {

        setError("");

        setStep(2);

      }

      return;

    }

    await form.handleSubmit(onSubmit)(event);

  }



  if (sessionCheckPending) {

    return <LoadingPanel height="sm" message="Loading..." className="border-0 bg-transparent shadow-none" />;

  }



  const showEmployerProgress = employer && (step === 2 || employerOnboarding);



  return (

    <form onSubmit={onFormSubmit} className="space-y-3">

      {showEmployerProgress ? (

        <div className="flex items-center gap-3">

          <span className="text-xs font-medium uppercase tracking-[0.12em] text-ink-secondary">

            Step {employerOnboarding || step === 2 ? 2 : 1} of 2

          </span>

          <span aria-hidden className="flex flex-1 gap-1.5">

            <span className="h-1 flex-1 rounded-full bg-primary" />

            <span className="h-1 flex-1 rounded-full bg-primary" />

          </span>

        </div>

      ) : employer ? (

        <div className="flex items-center gap-3">

          <span className="text-xs font-medium uppercase tracking-[0.12em] text-ink-secondary">Step 1 of 2</span>

          <span aria-hidden className="flex flex-1 gap-1.5">

            <span className="h-1 flex-1 rounded-full bg-primary" />

            <span className="h-1 flex-1 rounded-full bg-muted" />

          </span>

        </div>

      ) : null}

      {employerOnboarding ? (

        <>

          {signedInEmail ? (

            <p className="rounded-[var(--radius-control)] border border-input bg-muted/40 px-3 py-2 text-sm text-ink-secondary">

              Signed in as <span className="font-medium text-ink">{signedInEmail}</span>

            </p>

          ) : null}

          <RegisterCompanyStep register={form.register} errors={form.formState.errors} />

        </>

      ) : step === 1 ? (

        <>

          <RegisterRolePicker register={form.register} onRoleChange={handleRoleChange} />

          {!employer ? (

            <>

              <GoogleSignInButton label="Sign up with Google" role={role} size="sm" />

              <div className="relative">

                <div aria-hidden className="absolute inset-x-0 top-1/2 h-px bg-border" />

                <p className="relative mx-auto w-fit bg-surface px-2 text-[11px] uppercase tracking-[0.12em] text-ink-secondary">

                  or use email

                </p>

              </div>

            </>

          ) : null}

          <RegisterAccountStep

            register={form.register}

            errors={form.formState.errors}

            employer={employer}

            onRoleChange={handleRoleChange}

            showRole={false}

          />

        </>

      ) : (

        <RegisterCompanyStep register={form.register} errors={form.formState.errors} />

      )}

      {error ? (

        <p role="alert" className="text-sm text-danger">

          {error}

        </p>

      ) : null}

      <div className="flex flex-col gap-2.5 pt-0.5 sm:flex-row-reverse">
        <Button type="submit" disabled={form.formState.isSubmitting} className="w-full" size="sm">

          {form.formState.isSubmitting

            ? employerOnboarding

              ? "Submitting..."

              : "Creating account..."

            : employerOnboarding

              ? "Submit for approval"

              : employer && step === 1

                ? "Continue"

                : "Create account"}

        </Button>

        {step === 2 && !employerOnboarding ? (

          <Button type="button" variant="secondary" size="sm" className="w-full sm:w-auto sm:px-6" onClick={() => setStep(1)}>

            Back

          </Button>

        ) : null}

      </div>

    </form>

  );

}


