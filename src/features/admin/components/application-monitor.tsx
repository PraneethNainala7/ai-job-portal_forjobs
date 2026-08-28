"use client";

import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { TextField } from "@/components/ui/field";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { getAdminApplicationsRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";

export function ApplicationMonitor() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "applications"],
    queryFn: getAdminApplicationsRequest,
  });
  const [search, setSearch] = useState("");
  const items = useMemo(() => {
    return (data?.items ?? []).filter((item) => {
      const haystack = `${item.jobRole} ${item.candidateName} ${item.companyName} ${item.employerName ?? ""}`.toLowerCase();
      return !search || haystack.includes(search.toLowerCase());
    });
  }, [data, search]);

  if (isLoading) return <LoadingPanel height="md" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Applications could not be loaded.</p>;

  return (
    <div>
      <TextField
        label="Search applications"
        type="search"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        placeholder="Candidate, role, or company"
        wrapperClassName="mb-4 max-w-md"
      />
      <p className="mb-4 text-sm text-ink-secondary">Monitoring only. Shortlist and reject stay with the employer.</p>
      <div className="hidden overflow-hidden rounded-[var(--radius-card)] border bg-surface md:block">
        <table className="w-full text-left text-sm">
          <thead className="bg-muted text-ink-secondary">
            <tr>
              <th className="px-4 py-3 font-medium">Job</th>
              <th className="px-4 py-3 font-medium">Candidate</th>
              <th className="px-4 py-3 font-medium">Company</th>
              <th className="px-4 py-3 font-medium">Employer</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Applied</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="border-t">
                <td className="px-4 py-3">{item.jobRole}</td>
                <td className="px-4 py-3">{item.candidateName}</td>
                <td className="px-4 py-3">{item.companyName}</td>
                <td className="px-4 py-3">{item.employerName ?? "Not linked"}</td>
                <td className="px-4 py-3"><StatusBadge status={item.status} /></td>
                <td className="px-4 py-3">{formatDate(item.appliedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ul className="space-y-3 md:hidden">
        {items.map((item) => (
          <li key={item.id} className="rounded-[var(--radius-card)] border bg-surface p-4 text-sm">
            <p className="font-semibold">{item.candidateName}</p>
            <p className="mt-1 text-ink-secondary">{item.jobRole} at {item.companyName}</p>
          </li>
        ))}
      </ul>
    </div>
  );
}
