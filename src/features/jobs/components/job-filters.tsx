"use client";

import {
  BriefcaseIcon,
  CaretDownIcon,
  FileTextIcon,
  FunnelSimpleIcon,
  HouseIcon,
  ListDashesIcon,
  MagnifyingGlassIcon,
  MapPinIcon,
  XIcon,
} from "@phosphor-icons/react";
import { useRouter, useSearchParams } from "next/navigation";
import { useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { controlClass } from "@/components/ui/control-styles";
import { TextField } from "@/components/ui/field";
import { SelectMenu } from "@/components/ui/select-menu";
import { locationFilterOptions, resolveLocationFilter } from "@/lib/city-suggestions";
import { cn } from "@/lib/utils";

const jobTypes = [
  { value: "", label: "Any type", icon: ListDashesIcon },
  { value: "Full-time", label: "Full-time", icon: BriefcaseIcon },
  { value: "Contract", label: "Contract", icon: FileTextIcon },
];

const locations = [
  { value: "", label: "Any location", icon: ListDashesIcon },
  ...locationFilterOptions().map(({ value, label }) => ({
    value,
    label,
    icon: value === "Remote" ? HouseIcon : MapPinIcon,
  })),
];

type FilterState = {
  search: string;
  role: string;
  location: string;
  skills: string;
  experience: string;
  salary: string;
  jobType: string;
};

const empty: FilterState = {
  search: "",
  role: "",
  location: "",
  skills: "",
  experience: "",
  salary: "",
  jobType: "",
};

export function JobFilters({ basePath = "/jobs" }: { basePath?: string }) {
  const router = useRouter();
  const params = useSearchParams();
  const initial = useMemo(
    () => ({
      search: params.get("search") ?? "",
      role: params.get("role") ?? "",
      location: resolveLocationFilter(params.get("location") ?? ""),
      skills: params.get("skills") ?? "",
      experience: params.get("experience") ?? "",
      salary: params.get("salary") ?? "",
      jobType: params.get("jobType") ?? "",
    }),
    [params],
  );
  const [filters, setFilters] = useState<FilterState>(initial);
  const extraCount = [filters.role, filters.skills, filters.experience, filters.salary].filter(Boolean).length;
  const [moreOpen, setMoreOpen] = useState(extraCount > 0);

  const chips = useMemo(() => {
    const items: { key: keyof FilterState; label: string }[] = [];
    if (filters.location) {
      const locationLabel = locations.find((option) => option.value === filters.location)?.label ?? filters.location;
      items.push({ key: "location", label: locationLabel });
    }
    if (filters.jobType) items.push({ key: "jobType", label: filters.jobType });
    if (filters.role) items.push({ key: "role", label: filters.role });
    if (filters.skills) items.push({ key: "skills", label: filters.skills });
    if (filters.experience) items.push({ key: "experience", label: filters.experience });
    if (filters.salary) items.push({ key: "salary", label: filters.salary });
    return items;
  }, [filters]);

  function push(next: FilterState) {
    const query = new URLSearchParams();
    if (next.search) query.set("search", next.search);
    if (next.role) query.set("role", next.role);
    if (next.location) query.set("location", next.location);
    if (next.skills) query.set("skills", next.skills);
    if (next.experience) query.set("experience", next.experience);
    if (next.salary) query.set("salary", next.salary);
    if (next.jobType) query.set("jobType", next.jobType);
    const suffix = query.toString();
    router.push(suffix ? `${basePath}?${suffix}` : basePath);
  }

  function commit(partial: Partial<FilterState>) {
    const next = { ...filters, ...partial };
    setFilters(next);
    push(next);
  }

  function onSearch(event: React.FormEvent) {
    event.preventDefault();
    push(filters);
  }

  function reset() {
    setFilters(empty);
    setMoreOpen(false);
    router.push(basePath);
  }

  return (
    <form onSubmit={onSearch} className="space-y-3">
      <div className="flex flex-col gap-2 sm:flex-row">
        <label className="relative min-w-0 flex-1">
          <span className="sr-only">Search jobs</span>
          <MagnifyingGlassIcon
            size={18}
            aria-hidden
            className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted"
          />
          <input
            type="search"
            value={filters.search}
            onChange={(event) => setFilters((current) => ({ ...current, search: event.target.value }))}
            placeholder="Search role, company, or skill"
            className={cn(controlClass, "pl-10")}
          />
        </label>
        <Button type="submit" icon={MagnifyingGlassIcon} className="sm:w-auto">
          Search
        </Button>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <SelectMenu
          label="Location"
          menuLabel="Filter by location"
          hideLabel
          dense
          value={filters.location}
          onValueChange={(location) => commit({ location })}
          options={locations}
          wrapperClassName="w-full space-y-0 sm:w-52"
        />
        <SelectMenu
          label="Job type"
          menuLabel="Filter by type"
          hideLabel
          dense
          value={filters.jobType}
          onValueChange={(jobType) => commit({ jobType })}
          options={jobTypes}
          wrapperClassName="w-full space-y-0 sm:w-40"
        />
        <Button
          type="button"
          variant={moreOpen || extraCount ? "secondary" : "ghost"}
          size="sm"
          icon={FunnelSimpleIcon}
          onClick={() => setMoreOpen((open) => !open)}
          aria-expanded={moreOpen}
        >
          More
          <CaretDownIcon size={14} aria-hidden className={cn("transition-transform", moreOpen && "rotate-180")} />
          {extraCount ? (
            <span className="grid min-w-5 place-items-center rounded-full bg-primary px-1.5 text-[11px] font-semibold text-white">
              {extraCount}
            </span>
          ) : null}
        </Button>
        {chips.length || filters.search ? (
          <Button type="button" variant="ghost" size="sm" onClick={reset}>
            Clear
          </Button>
        ) : null}
      </div>

      {moreOpen ? (
        <div className="grid gap-3 rounded-[var(--radius-control)] border border-dashed border-input bg-muted/40 p-3 sm:grid-cols-2 lg:grid-cols-4">
          <TextField
            label="Role title"
            dense
            value={filters.role}
            onChange={(event) => setFilters((current) => ({ ...current, role: event.target.value }))}
            placeholder="Frontend, analyst"
          />
          <TextField
            label="Skills"
            dense
            value={filters.skills}
            onChange={(event) => setFilters((current) => ({ ...current, skills: event.target.value }))}
            placeholder="React, SQL"
          />
          <TextField
            label="Experience"
            dense
            value={filters.experience}
            onChange={(event) => setFilters((current) => ({ ...current, experience: event.target.value }))}
            placeholder="2-4 years"
          />
          <TextField
            label="Salary"
            dense
            value={filters.salary}
            onChange={(event) => setFilters((current) => ({ ...current, salary: event.target.value }))}
            placeholder="18-26 LPA"
          />
          <div className="sm:col-span-2 lg:col-span-4">
            <Button type="submit" size="sm">
              Apply extra filters
            </Button>
          </div>
        </div>
      ) : null}

      {chips.length ? (
        <ul className="flex flex-wrap gap-2" aria-label="Active filters">
          {chips.map((chip) => (
            <li key={chip.key}>
              <button
                type="button"
                onClick={() => commit({ [chip.key]: "" })}
                className="inline-flex h-8 items-center gap-1.5 rounded-full bg-primary-light px-2.5 text-xs font-medium text-primary hover:bg-primary/15"
              >
                {chip.label}
                <XIcon size={12} weight="bold" aria-hidden />
                <span className="sr-only">Remove {chip.label} filter</span>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </form>
  );
}
