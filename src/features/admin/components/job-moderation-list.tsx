"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useMemo, useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { TextField } from "@/components/ui/field";
import { SelectMenu } from "@/components/ui/select-menu";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { jobStatusOptions } from "@/features/admin/components/status-options";
import { getAdminJobsRequest } from "@/lib/api/admin";

export function JobModerationList() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "jobs"],
    queryFn: getAdminJobsRequest,
  });
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("ALL");
  const items = useMemo(() => {
    return (data?.items ?? []).filter((job) => {
      const haystack = `${job.role} ${job.companyName} ${job.employerName ?? ""} ${job.location}`.toLowerCase();
      if (search && !haystack.includes(search.toLowerCase())) return false;
      if (status !== "ALL" && job.status !== status) return false;
      return true;
    });
  }, [data, search, status]);

  if (isLoading) return <LoadingPanel height="md" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Jobs could not be loaded.</p>;

  return (
    <div>
      <div className="mb-4 grid gap-3 rounded-[var(--radius-card)] border bg-surface p-4 shadow-[var(--shadow-card)] md:grid-cols-2">
        <TextField
          label="Search jobs"
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Role, company, or location"
        />
        <SelectMenu
          label="Job status"
          menuLabel="Filter by status"
          value={status}
          onValueChange={setStatus}
          options={jobStatusOptions}
        />
      </div>
      <div className="hidden overflow-hidden rounded-[var(--radius-card)] border bg-surface md:block">
        <table className="w-full text-left text-sm">
          <thead className="bg-muted text-ink-secondary">
            <tr>
              <th className="px-4 py-3 font-medium">Role</th>
              <th className="px-4 py-3 font-medium">Company</th>
              <th className="px-4 py-3 font-medium">Employer</th>
              <th className="px-4 py-3 font-medium">Location</th>
              <th className="px-4 py-3 font-medium">Type</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Posted</th>
              <th className="px-4 py-3 font-medium">Applicants</th>
            </tr>
          </thead>
          <tbody>
            {items.map((job) => (
              <tr key={job.id} className="border-t">
                <td className="px-4 py-3">
                  <Link href={`/admin/jobs/${job.id}`} className="font-semibold text-primary">{job.role}</Link>
                </td>
                <td className="px-4 py-3">{job.companyName}</td>
                <td className="px-4 py-3">{job.employerName ?? "Not linked"}</td>
                <td className="px-4 py-3">{job.location}</td>
                <td className="px-4 py-3">{job.jobType}</td>
                <td className="px-4 py-3"><StatusBadge status={job.status} /></td>
                <td className="px-4 py-3">{job.postedDate}</td>
                <td className="px-4 py-3">{job.applicantCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ul className="space-y-3 md:hidden">
        {items.map((job) => (
          <li key={job.id} className="rounded-[var(--radius-card)] border bg-surface p-4">
            <Link href={`/admin/jobs/${job.id}`} className="font-semibold text-primary">{job.role}</Link>
            <p className="mt-1 text-sm text-ink-secondary">{job.companyName} · {job.location}</p>
          </li>
        ))}
      </ul>
    </div>
  );
}
