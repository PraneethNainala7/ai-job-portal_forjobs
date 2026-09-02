"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { ChipInput } from "@/components/ui/chip-input";
import { AutocompleteTextField } from "@/components/ui/autocomplete-text-field";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { Button } from "@/components/ui/button";
import { TextField } from "@/components/ui/field";
import { getProfileRequest, saveProfileRequest } from "@/lib/api/candidate";
import { CITY_SUGGESTIONS } from "@/lib/city-suggestions";

export function ProfileForm() {
  const queryClient = useQueryClient();
  const { data, isLoading, isError } = useQuery({ queryKey: ["profile"], queryFn: getProfileRequest });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const mutation = useMutation({
    mutationFn: saveProfileRequest,
    onSuccess: () => {
      setMessage("Profile saved.");
      setError("");
      void queryClient.invalidateQueries({ queryKey: ["profile"] });
    },
    onError: (err: Error) => {
      setError(err.message);
      setMessage("");
    },
  });

  if (isLoading) return <LoadingPanel height="lg" message="Loading your data..." />;
  if (isError || !data) return <p className="text-sm text-danger">Profile could not be loaded.</p>;

  return (
    <form
      className="space-y-8 rounded-[var(--radius-card)] border bg-surface p-6"
      onSubmit={(event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const skills = data.skills;
        mutation.mutate({
          fullName: String(form.get("fullName") ?? ""),
          phone: String(form.get("phone") ?? ""),
          location: String(form.get("location") ?? ""),
          title: String(form.get("title") ?? ""),
          experience: String(form.get("experience") ?? ""),
          linkedinUrl: String(form.get("linkedinUrl") ?? ""),
          portfolioUrl: String(form.get("portfolioUrl") ?? ""),
          education: String(form.get("education") ?? "").split(",").map((item) => item.trim()).filter(Boolean),
          certifications: String(form.get("certifications") ?? "").split(",").map((item) => item.trim()).filter(Boolean),
          skills,
        });
      }}
    >
      <section className="grid gap-5 md:grid-cols-2">
        <h2 className="text-lg font-semibold md:col-span-2">Personal information</h2>
        <TextField name="fullName" label="Full name" autoComplete="name" defaultValue={data.fullName} required />
        <TextField name="phone" label="Phone" type="tel" autoComplete="tel" defaultValue={data.phone} />
        <AutocompleteTextField name="location" label="Location" defaultValue={data.location} suggestions={CITY_SUGGESTIONS} />
        <p className="text-sm text-ink-secondary md:col-span-2">Email: {data.email}</p>
      </section>
      <section className="grid gap-5 md:grid-cols-2">
        <h2 className="text-lg font-semibold md:col-span-2">Professional information</h2>
        <TextField name="title" label="Professional title" defaultValue={data.title} />
        <TextField name="experience" label="Experience" placeholder="4 years" defaultValue={data.experience} />
        <TextField
          name="linkedinUrl"
          label="LinkedIn URL"
          type="url"
          inputMode="url"
          placeholder="https://linkedin.com/in/username"
          defaultValue={data.linkedinUrl}
        />
        <TextField
          name="portfolioUrl"
          label="Portfolio / GitHub URL"
          type="url"
          inputMode="url"
          placeholder="https://github.com/username"
          defaultValue={data.portfolioUrl}
        />
      </section>
      <ChipInput
        label="Skills"
        hint="Press Enter or use Add to include a skill. Skills drive your match scores."
        values={data.skills}
        onChange={(skills) => queryClient.setQueryData(["profile"], { ...data, skills })}
        addLabel="Add skill"
      />
      <TextField
        name="education"
        label="Education"
        hint="Separate multiple entries with commas."
        defaultValue={data.education?.join(", ")}
      />
      <TextField
        name="certifications"
        label="Certifications"
        hint="Separate multiple entries with commas."
        defaultValue={data.certifications?.join(", ")}
      />
      {message ? (
        <p role="status" className="text-sm" style={{ color: "var(--success-fg)" }}>
          {message}
        </p>
      ) : null}
      {error ? (
        <p role="alert" className="text-sm text-danger">
          {error}
        </p>
      ) : null}
      <Button type="submit" disabled={mutation.isPending}>
        {mutation.isPending ? "Saving..." : "Save changes"}
      </Button>
    </form>
  );
}
