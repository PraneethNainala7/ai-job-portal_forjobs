"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { PasswordField } from "@/components/ui/password-field";
import { Button } from "@/components/ui/button";
import {
  resetPasswordSchema,
  type ResetPasswordValues,
} from "@/features/auth/components/reset-password-schema";
import { resetPasswordRequest } from "@/lib/api/auth";

export function ResetPasswordForm() {
  const router = useRouter();
  const params = useSearchParams();
  const token = params.get("token")?.trim() ?? "";

  const form = useForm<ResetPasswordValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { password: "", confirmPassword: "" },
    mode: "onTouched",
    reValidateMode: "onChange",
  });

  async function onSubmit(values: ResetPasswordValues) {
    form.clearErrors();
    try {
      await resetPasswordRequest(token, values.password);
      router.replace("/login");
      router.refresh();
    } catch (err) {
      const message = err instanceof Error ? err.message : "Could not reset password.";
      form.setError("root", { type: "server", message });
    }
  }

  const { errors, isSubmitting } = form.formState;

  if (!token) {
    return (
      <div className="space-y-5">
        <p role="alert" className="text-sm text-danger">
          This reset link is invalid or missing. Request a new link to continue.
        </p>
        <Link href="/forgot-password" className="inline-block text-sm font-semibold text-primary">
          Request a new link
        </Link>
      </div>
    );
  }

  return (
    <form
      method="post"
      action="/reset-password"
      onSubmit={(event) => {
        event.preventDefault();
        void form.handleSubmit(onSubmit)(event);
      }}
      className="space-y-5"
      noValidate
    >
      <PasswordField
        label="New password"
        autoComplete="new-password"
        required
        dense
        error={errors.password?.message}
        {...form.register("password")}
      />
      <PasswordField
        label="Confirm password"
        autoComplete="new-password"
        placeholder="Re-enter password"
        required
        dense
        error={errors.confirmPassword?.message}
        {...form.register("confirmPassword")}
      />
      {errors.root?.message ? (
        <div className="space-y-2">
          <p role="alert" className="text-sm text-danger">
            {errors.root.message}
          </p>
          <Link href="/forgot-password" className="inline-block text-sm font-semibold text-primary">
            Request a new link
          </Link>
        </div>
      ) : null}
      <Button type="submit" disabled={isSubmitting} className="w-full">
        {isSubmitting ? "Updating..." : "Update password"}
      </Button>
    </form>
  );
}
