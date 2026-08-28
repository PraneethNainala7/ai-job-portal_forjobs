"use client";

import type { FieldErrors, UseFormRegister } from "react-hook-form";
import { TextField } from "@/components/ui/field";
import type { RegisterValues } from "@/features/auth/components/register-schema";
import type { UserRole } from "@/types/domain";

const roles: Array<{ value: UserRole; label: string }> = [
  { value: "CANDIDATE", label: "Find a job" },
  { value: "EMPLOYER", label: "Hire talent" },
];

export function RegisterAccountStep({
  register,
  errors,
  employer,
  onRoleChange,
}: {
  register: UseFormRegister<RegisterValues>;
  errors: FieldErrors<RegisterValues>;
  employer: boolean;
  onRoleChange: () => void;
}) {
  return (
    <>
      <fieldset>
        <legend className="text-sm font-medium text-ink">I want to</legend>
        <div className="mt-1.5 grid grid-cols-2 gap-2">
          {roles.map((option) => (
            <label
              key={option.value}
              className="flex h-11 cursor-pointer items-center justify-center rounded-[var(--radius-control)] border border-input text-sm font-medium text-ink-secondary transition-colors has-[:checked]:border-primary has-[:checked]:bg-primary-light has-[:checked]:text-primary lg:h-10"
            >
              <input
                type="radio"
                value={option.value}
                className="sr-only"
                {...register("role", { onChange: onRoleChange })}
              />
              {option.label}
            </label>
          ))}
        </div>
      </fieldset>
      <TextField
        label={employer ? "Recruiter name" : "Full name"}
        autoComplete="name"
        dense
        error={errors.name?.message}
        {...register("name")}
      />
      <TextField
        label="Email"
        type="email"
        autoComplete="email"
        dense
        error={errors.email?.message}
        {...register("email")}
      />
      <TextField
        label="Password"
        type="password"
        autoComplete="new-password"
        placeholder="At least 8 characters"
        dense
        error={errors.password?.message}
        {...register("password")}
      />
    </>
  );
}
