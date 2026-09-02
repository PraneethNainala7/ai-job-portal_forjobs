"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { useForm, type UseFormReturn } from "react-hook-form";
import { TextField } from "@/components/ui/field";
import { PasswordField } from "@/components/ui/password-field";
import { Button } from "@/components/ui/button";
import { getHomePath } from "@/config/routes";
import { GoogleRoleRetry, GoogleSignInButton } from "@/features/auth/components/google-sign-in-button";
import { loginSchema, type LoginValues } from "@/features/auth/components/login-schema";
import { AuthRequestError, loginRequest } from "@/lib/api/auth";

function applyLoginError(form: UseFormReturn<LoginValues>, err: unknown) {
  if (err instanceof AuthRequestError) {
    switch (err.code) {
      case "USER_NOT_FOUND":
        form.setError("email", { type: "server", message: err.message });
        return;
      case "WRONG_PASSWORD":
        form.setError("password", { type: "server", message: err.message });
        return;
      case "GOOGLE_ACCOUNT":
        form.setError("email", { type: "server", message: err.message });
        return;
      default:
        break;
    }
  }

  const message = err instanceof Error ? err.message : "Could not sign in.";
  const lower = message.toLowerCase();

  if (lower.includes("email is required") || lower.includes("enter a valid email") || lower.includes("no account found")) {
    form.setError("email", { type: "server", message });
    return;
  }

  if (lower.includes("password is required") || lower.includes("incorrect password")) {
    form.setError("password", { type: "server", message });
    return;
  }

  if (lower.includes("google sign-in")) {
    form.setError("email", { type: "server", message });
    return;
  }

  if (lower.includes("inactive")) {
    form.setError("root", { type: "server", message });
    return;
  }

  form.setError("root", { type: "server", message });
}

export function LoginForm() {
  const router = useRouter();
  const params = useSearchParams();
  const employerLogin = params.get("role") === "EMPLOYER";
  const [pendingRoleToken, setPendingRoleToken] = useState<string | null>(null);
  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
    mode: "onTouched",
    reValidateMode: "onChange",
  });

  async function onSubmit(values: LoginValues) {
    form.clearErrors();
    try {
      const { user } = await loginRequest(values.email, values.password);
      router.replace(getHomePath(user.role, user.accountStatus, { cin: user.cin }));
      router.refresh();
    } catch (err) {
      applyLoginError(form, err);
    }
  }

  const { errors, isSubmitting } = form.formState;

  return (
    <div className="space-y-5">
      {pendingRoleToken ? (
        <GoogleRoleRetry idToken={pendingRoleToken} onCancel={() => setPendingRoleToken(null)} />
      ) : (
        <>
          {!employerLogin ? (
            <>
              <GoogleSignInButton label="Sign in with Google" onNeedsRole={setPendingRoleToken} />
              <div className="relative py-1">
                <div aria-hidden className="absolute inset-x-0 top-1/2 h-px bg-border" />
                <p className="relative mx-auto w-fit bg-surface px-3 text-xs uppercase tracking-[0.12em] text-ink-secondary">
                  or use email
                </p>
              </div>
            </>
          ) : null}
          <form
            method="post"
            action="/login"
            onSubmit={(event) => {
              event.preventDefault();
              void form.handleSubmit(onSubmit)(event);
            }}
            className="space-y-5"
            noValidate
          >
            <TextField
              label="Email"
              type="email"
              autoComplete="email"
              inputMode="email"
              required
              dense
              error={errors.email?.message}
              {...form.register("email")}
            />
            <PasswordField
              label="Password"
              autoComplete="current-password"
              required
              dense
              error={errors.password?.message}
              {...form.register("password")}
            />
            {errors.root?.message ? (
              <p role="alert" className="text-sm text-danger">
                {errors.root.message}
              </p>
            ) : null}
            <Button type="submit" disabled={isSubmitting} className="w-full">
              {isSubmitting ? "Signing in..." : "Login"}
            </Button>
          </form>
        </>
      )}
    </div>
  );
}
