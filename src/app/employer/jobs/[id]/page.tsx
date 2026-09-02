import Link from "next/link";
import { notFound } from "next/navigation";
import { PageHeader } from "@/components/layout/page-header";
import { requireActiveEmployerPage } from "@/lib/auth/employer-page";
import { fetchEmployerJob } from "@/lib/api/server";
import { getJobPrimarySkills } from "@/lib/jobs";

export default async function EmployerJobDetailPage({
  params,
}: PageProps<"/employer/jobs/[id]">) {
  await requireActiveEmployerPage();
  const { id } = await params;
  const job = await fetchEmployerJob(id);
  if (!job) notFound();
  const primarySkills = getJobPrimarySkills(job);

  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Employer", href: "/employer/dashboard" },
          { label: "Manage jobs", href: "/employer/jobs" },
          { label: job.role },
        ]}
        title={job.role}
        description={`${job.location} · ${job.jobType} · ${job.status === "ACTIVE" ? "Active" : "Closed"}`}
      />
      <article className="rounded-[var(--radius-card)] border bg-surface p-6">
        <p className="text-sm text-ink-secondary">{job.applicantCount} applicants · {job.shortlistedCount} shortlisted or in interview</p>
        <p className="mt-4 text-sm leading-7 text-ink-secondary">{job.description}</p>
        {primarySkills.length ? (
          <>
            <p className="mt-6 text-sm font-medium text-ink">Required skills</p>
            <ul className="mt-2 flex flex-wrap gap-2">
              {primarySkills.map((skill) => (
                <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
              ))}
            </ul>
          </>
        ) : null}
        {job.preferredSkills?.length ? (
          <>
            <p className="mt-4 text-sm font-medium text-ink">Preferred skills</p>
            <ul className="mt-2 flex flex-wrap gap-2">
              {job.preferredSkills.map((skill) => (
                <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
              ))}
            </ul>
          </>
        ) : null}
        <div className="mt-8 flex flex-wrap gap-3">
          {job.status === "ACTIVE" ? (
            <Link href={`/employer/jobs/${job.id}/edit`} className="inline-flex h-11 items-center rounded-[var(--radius-control)] border px-4 text-sm font-semibold">
              Edit
            </Link>
          ) : null}
          <Link href={`/employer/jobs/${job.id}/applicants`} className="inline-flex h-11 items-center rounded-[var(--radius-control)] bg-primary px-4 text-sm font-semibold text-white">
            View applicants
          </Link>
        </div>
      </article>
    </>
  );
}
