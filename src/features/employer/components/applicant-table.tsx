"use client";



import { useQuery } from "@tanstack/react-query";

import Link from "next/link";

import { useMemo, useState } from "react";

import { CalendarBlankIcon, TargetIcon, TrendUpIcon } from "@phosphor-icons/react";

import { LoadingPanel } from "@/components/ui/loading-panel";
import { StatusBadge } from "@/features/admin/components/status-badge";

import { SelectMenu } from "@/components/ui/select-menu";

import { getApplicantsRequest } from "@/lib/api/employer";

import type { EmployerApplicant } from "@/types/domain";



type SortKey = "match" | "experience" | "date";



export function ApplicantTable({ jobId }: { jobId: string }) {

  const { data, isLoading, isError } = useQuery({

    queryKey: ["applicants", jobId],

    queryFn: () => getApplicantsRequest(jobId),

  });

  const [sort, setSort] = useState<SortKey>("match");

  const items = useMemo(() => {

    const list = [...(data?.items ?? [])];

    list.sort((a, b) => compareApplicants(a, b, sort));

    return list;

  }, [data, sort]);



  if (isLoading) return <LoadingPanel height="md" message="Loading applicants..." />;

  if (isError || !data) return <p className="text-sm text-danger">Applicants could not be loaded.</p>;

  if (!items.length) return <p className="rounded-[var(--radius-card)] border bg-surface p-6 text-sm text-ink-secondary">No applicants yet.</p>;



  return (

    <div>

      <SelectMenu

        label="Sort by"

        menuLabel="Order applicants by"

        value={sort}

        onValueChange={(next) => setSort(next as SortKey)}

        wrapperClassName="mb-4 max-w-xs"

        options={[

          { value: "match", label: "Match score", icon: TargetIcon },

          { value: "experience", label: "Experience", icon: TrendUpIcon },

          { value: "date", label: "Application date", icon: CalendarBlankIcon },

        ]}

      />

      <div className="hidden overflow-hidden rounded-[var(--radius-card)] border bg-surface md:block">

        <table className="w-full text-left text-sm">

          <thead className="bg-muted text-ink-secondary">

            <tr>

              <th className="px-4 py-3 font-medium">Candidate</th>

              <th className="px-4 py-3 font-medium">Match</th>

              <th className="px-4 py-3 font-medium">Experience</th>

              <th className="px-4 py-3 font-medium">Applied</th>

              <th className="px-4 py-3 font-medium">Status</th>

            </tr>

          </thead>

          <tbody>

            {items.map((item) => (

              <tr key={item.application.id} className="border-t">

                <td className="px-4 py-3">

                  <Link href={`/employer/jobs/${jobId}/applicants/${item.application.id}`} className="font-semibold text-primary">

                    {item.candidate.fullName}

                  </Link>

                </td>

                <td className="px-4 py-3">{formatMatchScore(item.match)}</td>

                <td className="px-4 py-3">{item.candidate.experience ?? "Not specified"}</td>

                <td className="px-4 py-3">{new Date(item.application.appliedAt).toLocaleDateString()}</td>

                <td className="px-4 py-3">

                  <StatusBadge status={item.application.status} />

                </td>

              </tr>

            ))}

          </tbody>

        </table>

      </div>

      <ul className="space-y-3 md:hidden">

        {items.map((item) => (

          <li key={item.application.id} className="rounded-[var(--radius-card)] border bg-surface p-4">

            <div className="flex items-start justify-between gap-3">

              <Link href={`/employer/jobs/${jobId}/applicants/${item.application.id}`} className="font-semibold text-primary">

                {item.candidate.fullName}

              </Link>

              <StatusBadge status={item.application.status} />

            </div>

            <p className="mt-2 text-sm text-ink-secondary">

              Match {formatMatchScore(item.match)} · {item.candidate.experience ?? "Experience n/a"}

            </p>

          </li>

        ))}

      </ul>

    </div>

  );

}



function compareApplicants(a: EmployerApplicant, b: EmployerApplicant, sort: SortKey) {

  if (sort === "match") return matchScore(b.match) - matchScore(a.match);

  if (sort === "date") return new Date(b.application.appliedAt).getTime() - new Date(a.application.appliedAt).getTime();

  return (b.candidate.experience ?? "").localeCompare(a.candidate.experience ?? "");

}



function matchScore(match: EmployerApplicant["match"]) {

  return match?.matchScore ?? -1;

}



function formatMatchScore(match: EmployerApplicant["match"]) {

  return match?.matchScore != null ? `${match.matchScore}%` : "—";

}


