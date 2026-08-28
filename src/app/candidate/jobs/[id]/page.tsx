import Link from "next/link";
import { notFound } from "next/navigation";
import { PageHeader } from "@/components/layout/page-header";
import { ApplyButton } from "@/features/candidate/components/apply-button";
import { MatchPanel } from "@/features/candidate/components/match-panel";
import { fetchPublicJob } from "@/lib/api/server";

export default async function CandidateJobDetailPage({
  params,
}: PageProps<"/candidate/jobs/[id]">) {
  const { id } = await params;
  const job = await fetchPublicJob(id);
  if (!job) notFound();

  return (
    <>
      <PageHeader
        crumbs={[
          { label: "Candidate", href: "/candidate/dashboard" },
          { label: "Find jobs", href: "/candidate/jobs" },
          { label: job.role },
        ]}
        title={job.role}
        description={`${job.companyName} · ${job.location} · ${job.jobType}`}
      />
      <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
        <article className="rounded-[var(--radius-card)] border bg-surface p-6">
          <dl className="grid gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-xs uppercase text-ink-muted">Experience</dt>
              <dd className="mt-1 text-sm font-medium">{job.experience}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase text-ink-muted">Salary</dt>
              <dd className="mt-1 text-sm font-medium">{job.salary}</dd>
            </div>
          </dl>
          <h2 className="mt-8 text-xl font-semibold">About the role</h2>
          <p className="mt-3 text-sm leading-7 text-ink-secondary">{job.description}</p>
          <h2 className="mt-8 text-xl font-semibold">Required skills</h2>
          <ul className="mt-3 flex flex-wrap gap-2">
            {job.skills.map((skill) => (
              <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
            ))}
          </ul>
        </article>
        <aside className="space-y-4 lg:sticky lg:top-6 lg:h-fit">
          <MatchPanel jobId={job.id} />
          <div className="rounded-[var(--radius-card)] border bg-surface p-5">
            <ApplyButton jobId={job.id} />
            <Link href="/candidate/jobs" className="mt-3 block text-center text-sm font-medium text-primary">
              Back to jobs
            </Link>
          </div>
        </aside>
      </div>
    </>
  );
}
