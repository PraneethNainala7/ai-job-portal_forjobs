"use client";



import type { FieldErrors, UseFormRegister } from "react-hook-form";

import { TextField } from "@/components/ui/field";

import { PasswordField } from "@/components/ui/password-field";

import { RegisterRolePicker } from "@/features/auth/components/register-role-picker";

import type { RegisterValues } from "@/features/auth/components/register-schema";



export function RegisterAccountStep({

  register,

  errors,

  employer,

  onRoleChange,

  showRole = true,

}: {

  register: UseFormRegister<RegisterValues>;

  errors: FieldErrors<RegisterValues>;

  employer: boolean;

  onRoleChange: () => void;

  showRole?: boolean;

}) {

  return (
    <>
      {showRole ? <RegisterRolePicker register={register} onRoleChange={onRoleChange} /> : null}
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
      <PasswordField
        label="Password"
        autoComplete="new-password"
        placeholder="At least 8 characters"
        dense
        error={errors.password?.message}
        {...register("password")}
      />
      <PasswordField
        label="Confirm password"
        autoComplete="new-password"
        placeholder="Re-enter password"
        dense
        error={errors.confirmPassword?.message}
        {...register("confirmPassword")}
      />
    </>
  );

}

