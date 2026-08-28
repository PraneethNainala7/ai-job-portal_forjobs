"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { TextAreaField, TextField } from "@/components/ui/field";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { Button } from "@/components/ui/button";
import {
  getEmployerProfileRequest,
  resubmitEmployerRequest,
  saveEmployerProfileRequest,
} from "@/lib/api/employer";

export function CompanyProfileForm({ canResubmit }: { canResubmit: boolean }) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data, isLoading, isError } = useQuery({ queryKey: ["employer-profile"], queryFn: getEmployerProfileRequest });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const save = useMutation({
    mutationFn: saveEmployerProfileRequest,
    onSuccess: () => {
      setMessage("Company profile saved.");
      setError("");
      void queryClient.invalidateQueries({ queryKey: ["employer-profile"] });
      router.refresh();
    },
    onError: (err: Error) => {
      setError(err.message);
      setMessage("");
    },
  });
  const resubmit = useMutation({
    mutationFn: resubmitEmployerRequest,
    onSuccess: () => router.replace("/employer/pending"),
    onError: (err: Error) => setError(err.message),
  });

  if (isLoading) return <LoadingPanel height="lg" message="Loading your data..." />;
  if (isError || !data) return <p className="text-sm text-danger">Company profile could not be loaded.</p>;

  return (
    <form
      className="space-y-6 rounded-[var(--radius-card)] border bg-surface p-6"
      onSubmit={(event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        save.mutate({
          companyName: String(form.get("companyName") ?? ""),
          companyInformation: String(form.get("companyInformation") ?? ""),
          companyLocation: String(form.get("companyLocation") ?? ""),
          companyWebsite: String(form.get("companyWebsite") ?? ""),
        });
      }}
    >
      <TextField
        name="companyName"
        label="Company name"
        autoComplete="organization"
        required
        defaultValue={data.companyName}
      />
      <TextAreaField
        name="companyInformation"
        label="Company information"
        rows={5}
        required
        hint="Shown to candidates and used by admin during review."
        defaultValue={data.companyInformation}
      />
      <div className="grid gap-5 md:grid-cols-2">
        <TextField name="companyLocation" label="Location" required defaultValue={data.companyLocation} />
        <TextField
          name="companyWebsite"
          label="Website"
          type="url"
          inputMode="url"
          placeholder="https://example.com"
          defaultValue={data.companyWebsite}
        />
      </div>
      {data.cin ? (
        <p className="text-sm text-ink-secondary">
          CIN: <span className="font-mono text-xs">{data.cin}</span>
        </p>
      ) : null}
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
      <div className="flex flex-col gap-3 sm:flex-row">
        <Button type="submit" disabled={save.isPending}>
          {save.isPending ? "Saving..." : "Save company profile"}
        </Button>
        {canResubmit ? (
          <Button type="button" variant="secondary" disabled={resubmit.isPending} onClick={() => resubmit.mutate()}>
            {resubmit.isPending ? "Resubmitting..." : "Resubmit for approval"}
          </Button>
        ) : null}
      </div>
    </form>
  );
}
