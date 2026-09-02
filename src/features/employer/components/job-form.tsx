"use client";



import { useMutation } from "@tanstack/react-query";

import { useRouter } from "next/navigation";

import { useState } from "react";

import { BriefcaseIcon, ClockIcon, FileTextIcon, HouseIcon, MapPinIcon, UsersThreeIcon } from "@phosphor-icons/react";

import { ChipInput } from "@/components/ui/chip-input";

import { Button } from "@/components/ui/button";

import { TextAreaField, TextField } from "@/components/ui/field";
import { AutocompleteTextField } from "@/components/ui/autocomplete-text-field";

import { SelectMenu } from "@/components/ui/select-menu";

import { createEmployerJobRequest, updateEmployerJobRequest } from "@/lib/api/employer";
import { SKILL_SUGGESTIONS } from "@/lib/skill-suggestions";
import { CITY_SUGGESTIONS } from "@/lib/city-suggestions";

import type { Job } from "@/types/domain";



const types = [

  { value: "Full-time", label: "Full-time", icon: BriefcaseIcon },

  { value: "Contract", label: "Contract", icon: FileTextIcon },

  { value: "Part-time", label: "Part-time", icon: ClockIcon },

];

const modes = [

  { value: "Hybrid", label: "Hybrid", icon: UsersThreeIcon },

  { value: "Remote", label: "Remote", icon: HouseIcon },

  { value: "On-site", label: "On-site", icon: MapPinIcon },

];



export function JobForm({ job }: { job?: Job }) {

  const router = useRouter();

  const [required, setRequired] = useState(
    job?.skills?.length ? job.skills : (job?.criticalSkills ?? []),
  );
  const [critical, setCritical] = useState(job?.criticalSkills ?? []);
  const [preferred, setPreferred] = useState(job?.preferredSkills ?? []);
  const [educationRequirements, setEducationRequirements] = useState(job?.educationRequirements ?? []);
  const [certificationRequirements, setCertificationRequirements] = useState(job?.certificationRequirements ?? []);

  const [error, setError] = useState("");

  const mutation = useMutation({

    mutationFn: (input: unknown) => (job ? updateEmployerJobRequest(job.id, input) : createEmployerJobRequest(input)),

    onSuccess: (saved) => router.push(`/employer/jobs/${saved.id}`),

    onError: (err: Error) => setError(err.message),

  });



  return (

    <form

      className="space-y-6 rounded-[var(--radius-card)] border bg-surface p-6"

      onSubmit={(event) => {

        event.preventDefault();

        if (required.length === 0 && critical.length === 0) {

          setError("Add at least one required skill.");

          return;

        }

        setError("");

        const form = new FormData(event.currentTarget);

        mutation.mutate({

          role: String(form.get("role") ?? ""),

          experience: String(form.get("experience") ?? ""),

          location: String(form.get("location") ?? ""),

          salary: String(form.get("salary") ?? ""),

          jobType: String(form.get("jobType") ?? ""),

          workMode: String(form.get("workMode") ?? ""),

          description: String(form.get("description") ?? ""),

          skills: required,

          criticalSkills: critical,

          preferredSkills: preferred,

          educationRequirements,

          certificationRequirements,

        });

      }}

    >

      <div className="grid gap-5 md:grid-cols-2">

        <TextField name="role" label="Role" defaultValue={job?.role} required />

        <TextField name="experience" label="Experience" placeholder="2-4 years" defaultValue={job?.experience} required />

        <AutocompleteTextField name="location" label="Location" placeholder="Bengaluru, India" defaultValue={job?.location} required suggestions={CITY_SUGGESTIONS} />

        <TextField name="salary" label="Salary" placeholder="18-26 LPA" defaultValue={job?.salary} required />

        <SelectMenu

          name="jobType"

          label="Job type"

          menuLabel="Select job type"

          defaultValue={job?.jobType ?? "Full-time"}

          options={types}

        />

        <SelectMenu

          name="workMode"

          label="Work mode"

          menuLabel="Select work mode"

          defaultValue={job?.workMode ?? "Hybrid"}

          options={modes}

        />

      </div>

      <ChipInput
        label="Required skills"
        hint="Must-have skills for this role. Paste a list (one per line or comma-separated), pick from suggestions, or type and press Enter."
        values={required}
        onChange={setRequired}
        suggestions={SKILL_SUGGESTIONS}
        addLabel="Add skill"
      />

      <ChipInput
        label="Critical skills (must-have)"
        hint="Optional subset of required skills. Missing all critical skills caps the match score."
        values={critical}
        onChange={setCritical}
        suggestions={SKILL_SUGGESTIONS.filter(
          (skill) => required.some((item) => item.toLowerCase() === skill.toLowerCase()),
        )}
        addLabel="Add skill"
      />

      <ChipInput
        label="Preferred skills"
        hint="Optional nice-to-have skills."
        values={preferred}
        onChange={setPreferred}
        suggestions={SKILL_SUGGESTIONS.filter(
          (skill) => !required.some((item) => item.toLowerCase() === skill.toLowerCase()),
        )}
        addLabel="Add skill"
      />

      <ChipInput
        label="Education requirements"
        hint="Optional degrees or qualifications used in match scoring."
        values={educationRequirements}
        onChange={setEducationRequirements}
        addLabel="Add requirement"
      />

      <ChipInput
        label="Certification requirements"
        hint="Optional certifications used in match scoring."
        values={certificationRequirements}
        onChange={setCertificationRequirements}
        addLabel="Add requirement"
      />

      <TextAreaField

        name="description"

        label="Description"

        rows={6}

        required

        minLength={20}

        hint="At least 20 characters."

        defaultValue={job?.description}

      />

      {error ? (

        <p role="alert" className="text-sm text-danger">

          {error}

        </p>

      ) : null}

      <Button type="submit" disabled={mutation.isPending}>

        {mutation.isPending ? "Saving..." : job ? "Save job" : "Create job"}

      </Button>

    </form>

  );

}


