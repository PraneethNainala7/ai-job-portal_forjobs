"use client";

import type { FieldErrors, UseFormRegister, FieldValues, Path } from "react-hook-form";
import { TextAreaField, TextField } from "@/components/ui/field";
import { AutocompleteTextField } from "@/components/ui/autocomplete-text-field";
import { CITY_SUGGESTIONS } from "@/lib/city-suggestions";

type CompanyFields = {
  companyName?: string;
  companyLocation?: string;
  cin?: string;
  companyInformation?: string;
};

export function RegisterCompanyStep<T extends FieldValues & CompanyFields>({
  register,
  errors,
}: {
  register: UseFormRegister<T>;
  errors: FieldErrors<T>;
}) {
  return (
    <>
      <TextField
        label="Company name"
        autoComplete="organization"
        dense
        error={errors.companyName?.message as string | undefined}
        {...register("companyName" as Path<T>)}
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <AutocompleteTextField
          label="Company location"
          dense
          suggestions={CITY_SUGGESTIONS}
          error={errors.companyLocation?.message as string | undefined}
          {...register("companyLocation" as Path<T>)}
        />
        <TextField
          label="CIN"
          placeholder="Mandatory"
          dense
          error={errors.cin?.message as string | undefined}
          {...register("cin" as Path<T>)}
        />
      </div>
      <TextAreaField
        label="Company information"
        rows={3}
        dense
        error={errors.companyInformation?.message as string | undefined}
        {...register("companyInformation" as Path<T>)}
      />
    </>
  );
}
