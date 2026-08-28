"use client";

import type { FieldErrors, UseFormRegister } from "react-hook-form";
import { TextAreaField, TextField } from "@/components/ui/field";
import type { RegisterValues } from "@/features/auth/components/register-schema";

export function RegisterCompanyStep({
  register,
  errors,
}: {
  register: UseFormRegister<RegisterValues>;
  errors: FieldErrors<RegisterValues>;
}) {
  return (
    <>
      <TextField
        label="Company name"
        autoComplete="organization"
        dense
        error={errors.companyName?.message}
        {...register("companyName")}
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <TextField
          label="Company location"
          dense
          error={errors.companyLocation?.message}
          {...register("companyLocation")}
        />
        <TextField
          label="CIN"
          placeholder="Mandatory"
          dense
          error={errors.cin?.message}
          {...register("cin")}
        />
      </div>
      <TextAreaField
        label="Company information"
        rows={3}
        dense
        error={errors.companyInformation?.message}
        {...register("companyInformation")}
      />
    </>
  );
}
