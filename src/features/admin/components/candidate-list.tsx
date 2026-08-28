"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useMemo, useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { TextField } from "@/components/ui/field";
import { SelectMenu } from "@/components/ui/select-menu";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { candidateStatusOptions } from "@/features/admin/components/status-options";
import { getAdminCandidatesRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";

export function CandidateList() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", "candidates"],
    queryFn: getAdminCandidatesRequest,
  });
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("ALL");
  const items = useMemo(() => {
    return (data?.items ?? []).filter((item) => {
      const haystack = `${item.name} ${item.email} ${item.title ?? ""} ${item.location ?? ""}`.toLowerCase();
      if (search && !haystack.includes(search.toLowerCase())) return false;
      if (status !== "ALL" && item.accountStatus !== status) return false;
      return true;
    });
  }, [data, search, status]);

  if (isLoading) return <LoadingPanel height="md" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Candidates could not be loaded.</p>;

  return (
    <div>
      <Filters search={search} setSearch={setSearch} status={status} setStatus={setStatus} />
      {!items.length ? <p className="text-sm text-ink-secondary">No candidates match these filters.</p> : null}
      <div className="hidden overflow-hidden rounded-[var(--radius-card)] border bg-surface md:block">
        <table className="w-full text-left text-sm">
          <thead className="bg-muted text-ink-secondary">
            <tr>
              <th className="px-4 py-3 font-medium">Name</th>
              <th className="px-4 py-3 font-medium">Email</th>
              <th className="px-4 py-3 font-medium">Title</th>
              <th className="px-4 py-3 font-medium">Location</th>
              <th className="px-4 py-3 font-medium">Experience</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Registered</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="border-t">
                <td className="px-4 py-3">
                  <Link href={`/admin/candidates/${item.id}`} className="font-semibold text-primary">{item.name}</Link>
                </td>
                <td className="px-4 py-3">{item.email}</td>
                <td className="px-4 py-3">{item.title ?? "Not specified"}</td>
                <td className="px-4 py-3">{item.location ?? "Not specified"}</td>
                <td className="px-4 py-3">{item.experience ?? "Not specified"}</td>
                <td className="px-4 py-3"><StatusBadge status={item.accountStatus} /></td>
                <td className="px-4 py-3">{formatDate(item.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ul className="space-y-3 md:hidden">
        {items.map((item) => (
          <li key={item.id} className="rounded-[var(--radius-card)] border bg-surface p-4">
            <Link href={`/admin/candidates/${item.id}`} className="font-semibold text-primary">{item.name}</Link>
            <p className="mt-1 text-sm text-ink-secondary">{item.email}</p>
            <div className="mt-2"><StatusBadge status={item.accountStatus} /></div>
          </li>
        ))}
      </ul>
    </div>
  );
}

function Filters({
  search,
  setSearch,
  status,
  setStatus,
}: {
  search: string;
  setSearch: (value: string) => void;
  status: string;
  setStatus: (value: string) => void;
}) {
  return (
    <div className="mb-4 grid gap-3 rounded-[var(--radius-card)] border bg-surface p-4 shadow-[var(--shadow-card)] md:grid-cols-2">
      <TextField
        label="Search candidates"
        type="search"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        placeholder="Name, email, or title"
      />
      <SelectMenu
        label="Account status"
        menuLabel="Filter by status"
        value={status}
        onValueChange={setStatus}
        options={candidateStatusOptions}
      />
    </div>
  );
}
