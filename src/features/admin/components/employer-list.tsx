"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useMemo, useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { TextField } from "@/components/ui/field";
import { SelectMenu } from "@/components/ui/select-menu";
import { StatusBadge } from "@/features/admin/components/status-badge";
import { employerStatusOptions } from "@/features/admin/components/status-options";
import { getAdminEmployersRequest, getPendingEmployersRequest } from "@/lib/api/admin";
import { formatDate } from "@/lib/format";
import type { AdminAccount } from "@/types/domain";

export function EmployerList({ pendingOnly = false }: { pendingOnly?: boolean }) {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["admin", pendingOnly ? "pending-employers" : "employers"],
    queryFn: pendingOnly ? getPendingEmployersRequest : getAdminEmployersRequest,
  });
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("ALL");
  const items = useMemo(() => {
    return (data?.items ?? []).filter((item) => matchesEmployer(item, search, pendingOnly ? "PENDING" : status));
  }, [data, search, status, pendingOnly]);

  if (isLoading) return <LoadingPanel height="md" message="Loading records..." />;
  if (isError || !data) return <p className="text-sm text-danger">Employers could not be loaded.</p>;
  if (!items.length) {
    return (
      <div>
        {!pendingOnly ? <EmployerFilters search={search} setSearch={setSearch} status={status} setStatus={setStatus} /> : null}
        <p className="rounded-[var(--radius-card)] border bg-surface p-6 text-sm text-ink-secondary">
          {pendingOnly ? "No employer registrations are waiting for review." : "No employers match these filters."}
        </p>
      </div>
    );
  }

  return (
    <div>
      {!pendingOnly ? (
        <EmployerFilters search={search} setSearch={setSearch} status={status} setStatus={setStatus} />
      ) : (
        <TextField
          label="Search pending employers"
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Recruiter, company, or CIN"
          wrapperClassName="mb-4 max-w-md"
        />
      )}
      <ul className="space-y-3">
        {items.map((item) => (
          <li key={item.id} className="rounded-[var(--radius-card)] border bg-surface p-5">
            <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
              <div>
                <Link href={`/admin/employers/${item.id}`} className="text-lg font-semibold text-primary">{item.companyName ?? item.name}</Link>
                <p className="mt-1 text-sm text-ink-secondary">
                  Recruiter: {item.name} · {item.email} · {item.companyLocation ?? "Location not provided"}
                </p>
                <p className="mt-1 text-sm text-ink-secondary">Registered {formatDate(item.createdAt)}</p>
              </div>
              <StatusBadge status={item.accountStatus} />
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

function matchesEmployer(item: AdminAccount, search: string, status: string) {
  const haystack = `${item.name} ${item.email} ${item.companyName ?? ""} ${item.companyLocation ?? ""} ${item.cin ?? ""}`.toLowerCase();
  if (search && !haystack.includes(search.toLowerCase())) return false;
  if (status !== "ALL" && item.accountStatus !== status) return false;
  return true;
}

function EmployerFilters({
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
        label="Search employers"
        type="search"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        placeholder="Recruiter, company, or CIN"
      />
      <SelectMenu
        label="Account status"
        menuLabel="Filter by status"
        value={status}
        onValueChange={setStatus}
        options={employerStatusOptions}
      />
    </div>
  );
}
