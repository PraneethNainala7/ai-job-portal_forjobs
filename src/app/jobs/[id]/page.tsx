import Link from "next/link";
import { notFound } from "next/navigation";
import { PublicFooter, PublicHeader } from "@/components/layout/public-chrome";
import { buttonClass } from "@/components/ui/button";
import { cardClass, chipClass } from "@/components/ui/control-styles";
import { getHomePath } from "@/config/routes";
import { getSession } from "@/lib/auth/session";
import { fetchPublicJob } from "@/lib/api/server";
import { getJobPrimarySkills } from "@/lib/jobs";
import { cn } from "@/lib/utils";

export const dynamic = "force-dynamic";

export default async function JobDetailPage({ params }: PageProps<"/jobs/[id]">) {
  const { id } = await params;
  const job = await fetchPublicJob(id);
  if (!job) notFound();
  const session = await getSession();
  const applyHref = session?.role === "CANDIDATE" && session.accountStatus === "ACTIVE"
    ? `/candidate/jobs/${job.id}`
    : `/login?next=/jobs/${job.id}`;
  const primarySkills = getJobPrimarySkills(job);

  return (
    <div className="flex min-h-[100dvh] flex-col bg-canvas">
      <PublicHeader />
      <article className="mx-auto grid w-full max-w-[1400px] flex-1 gap-6 px-4 py-8 md:grid-cols-[1fr_20rem] md:px-6 lg:px-8">
        <div className={cn(cardClass, "p-6 md:p-8")}>
          <nav className="text-[13px] text-ink-secondary">
            <Link href="/jobs">Jobs</Link>
            <span className="mx-2">/</span>
            <span className="text-ink">{job.role}</span>
          </nav>
          <h1 className="mt-4 text-[32px] font-bold leading-tight">{job.role}</h1>
          <p className="mt-2 text-sm text-ink-secondary">
            {job.companyName} · {job.location} · {job.workMode}
          </p>
          <dl className="mt-6 grid gap-4 sm:grid-cols-2">
            <Meta label="Experience" value={job.experience} />
            <Meta label="Job type" value={job.jobType} />
            <Meta label="Salary" value={job.salary} highlight />
            <Meta label="Posted" value={job.postedDate ?? "Recently"} />
          </dl>
          <h2 className="mt-8 text-xl font-semibold">About the role</h2>
          <p className="mt-3 text-sm leading-7 text-ink-secondary">{job.description}</p>
          {primarySkills.length ? (
            <>
              <h2 className="mt-8 text-xl font-semibold">Skills</h2>
              <SkillList skills={primarySkills} />
            </>
          ) : null}
          {job.preferredSkills?.length ? (
            <>
              <h2 className="mt-8 text-xl font-semibold">Preferred skills</h2>
              <SkillList skills={job.preferredSkills} />
            </>
          ) : null}
        </div>
        <aside className={cn(cardClass, "h-fit p-6 md:sticky md:top-6")}>
          {session?.role === "CANDIDATE" ? (
            <div className="mb-4 rounded-[var(--radius-card)] border border-ai-border bg-ai-surface p-4">
              <p className="text-sm font-semibold text-ai">AI match analysis</p>
              <p className="mt-2 text-sm leading-6 text-ink-secondary">
                Personalized match scores will appear after resume analysis in the candidate module.
              </p>
            </div>
          ) : null}
          <p className="text-sm text-ink-secondary">Ready to apply? Sign in as a candidate to submit an application.</p>
          <Link href={applyHref} className={cn(buttonClass(), "mt-4 w-full")}>
            {session?.role === "CANDIDATE" ? "Continue to apply" : "Login to apply"}
          </Link>
          {session && session.role !== "CANDIDATE" ? (
            <Link href={getHomePath(session.role, session.accountStatus)} className="mt-3 block text-center text-sm font-medium text-primary">
              Back to workspace
            </Link>
          ) : null}
        </aside>
      </article>
      <PublicFooter />
    </div>
  );
}

function Meta({ label, value, highlight }: { label: string; value: string; highlight?: boolean }) {
  return (
    <div className={cn("rounded-[var(--radius-control)] bg-muted/70 px-3 py-3", highlight && "bg-primary-light")}>
      <dt className="text-xs font-medium uppercase tracking-wide text-ink-muted">{label}</dt>
      <dd className={cn("mt-1 text-sm font-medium", highlight && "text-primary")}>{value}</dd>
    </div>
  );
}

function SkillList({ skills }: { skills: string[] }) {
  return (
    <ul className="mt-3 flex flex-wrap gap-2">
      {skills.map((skill) => (
        <li key={skill} className={chipClass}>
          {skill}
        </li>
      ))}
    </ul>
  );
}
