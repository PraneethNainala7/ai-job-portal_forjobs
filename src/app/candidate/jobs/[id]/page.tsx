import Link from "next/link";
import { notFound } from "next/navigation";
import { PageHeader } from "@/components/layout/page-header";
import { ApplyButton } from "@/features/candidate/components/apply-button";
import { MatchPanel } from "@/features/candidate/components/match-panel";
import { fetchPublicJob } from "@/lib/api/server";
import { getJobPrimarySkills } from "@/lib/jobs";

export default async function CandidateJobDetailPage({
  params,
}: PageProps<"/candidate/jobs/[id]">) {
  const { id } = await params;
  const job = await fetchPublicJob(id);
  if (!job) notFound();
  const primarySkills = getJobPrimarySkills(job);

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
          {primarySkills.length ? (
            <>
              <h2 className="mt-8 text-xl font-semibold">Required skills</h2>
              <ul className="mt-3 flex flex-wrap gap-2">
                {primarySkills.map((skill) => (
                  <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
                ))}
              </ul>
            </>
          ) : null}
          {job.preferredSkills?.length ? (
            <>
              <h2 className="mt-8 text-xl font-semibold">Preferred skills</h2>
              <ul className="mt-3 flex flex-wrap gap-2">
                {job.preferredSkills.map((skill) => (
                  <li key={skill} className="rounded-full bg-muted px-3 py-1.5 text-sm">{skill}</li>
                ))}
              </ul>
            </>
          ) : null}
          {job.educationRequirements?.length ? (
            <>
              <h2 className="mt-8 text-xl font-semibold">Education requirements</h2>
              <ul className="mt-3 flex flex-wrap gap-2">
                {job.educationRequirements.map((item) => (
                  <li key={item} className="rounded-full bg-muted px-3 py-1.5 text-sm">{item}</li>
                ))}
              </ul>
            </>
          ) : null}
          {job.certificationRequirements?.length ? (
            <>
              <h2 className="mt-8 text-xl font-semibold">Certification requirements</h2>
              <ul className="mt-3 flex flex-wrap gap-2">
                {job.certificationRequirements.map((item) => (
                  <li key={item} className="rounded-full bg-muted px-3 py-1.5 text-sm">{item}</li>
                ))}
              </ul>
            </>
          ) : null}
        </article>
        <aside className="space-y-4 lg:sticky lg:top-6 lg:h-fit">
          <div className="rounded-[var(--radius-card)] border bg-surface p-5">
            <ApplyButton jobId={job.id} />
            <Link href="/candidate/jobs" className="mt-3 block text-center text-sm font-medium text-primary">
              Back to jobs
            </Link>
          </div>
          <MatchPanel jobId={job.id} />
        </aside>
      </div>
    </>
  );
}
