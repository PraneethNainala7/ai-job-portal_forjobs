"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { TextField } from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import {
  forgotPasswordSchema,
  type ForgotPasswordValues,
} from "@/features/auth/components/forgot-password-schema";
import { forgotPasswordRequest } from "@/lib/api/auth";

export function ForgotPasswordForm() {
  const [submitted, setSubmitted] = useState(false);
  const form = useForm<ForgotPasswordValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: "" },
    mode: "onTouched",
    reValidateMode: "onChange",
  });

  async function onSubmit(values: ForgotPasswordValues) {
    form.clearErrors();
    try {
      await forgotPasswordRequest(values.email);
      setSubmitted(true);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Could not send reset link.";
      form.setError("root", { type: "server", message });
    }
  }

  const { errors, isSubmitting } = form.formState;

  if (submitted) {
    return (
      <div className="space-y-5">
        <p className="text-sm text-ink-secondary">
          If an account exists for that email, we sent a reset link. Check your inbox.
        </p>
        <Link href="/login" className="inline-block text-sm font-semibold text-primary">
          Back to login
        </Link>
      </div>
    );
  }

  return (
    <form
      method="post"
      action="/forgot-password"
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
      {errors.root?.message ? (
        <p role="alert" className="text-sm text-danger">
          {errors.root.message}
        </p>
      ) : null}
      <Button type="submit" disabled={isSubmitting} className="w-full">
        {isSubmitting ? "Sending..." : "Send reset link"}
      </Button>
    </form>
  );
}
