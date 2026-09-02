import { ArrowRightIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { buttonClass } from "@/components/ui/button";
import { cardClass } from "@/components/ui/control-styles";
import { EmployerHomeJobCard } from "@/features/home/employer-home-job-card";
import { JobCard } from "@/features/jobs/components/job-card";
import { hasAppliedToJob } from "@/lib/application-status";
import {
  fetchCandidateApplications,
  fetchEmployerJobs,
  fetchPublicJobsResult,
} from "@/lib/api/server";
import { getSession } from "@/lib/auth/session";
import { cn } from "@/lib/utils";

function FeaturedJobsShell({
  title,
  viewAllHref,
  viewAllLabel = "View all",
  children,
}: {
  title: string;
  viewAllHref: string;
  viewAllLabel?: string;
  children: React.ReactNode;
}) {
  return (
    <section className="mx-auto w-full max-w-[1400px] px-4 py-10 md:px-6 lg:px-8">
      <div className="mb-6 flex items-end justify-between gap-4">
        <h2 className="text-2xl font-bold">{title}</h2>
        <Link href={viewAllHref} className={cn(buttonClass({ variant: "link" }), "text-sm")}>
          {viewAllLabel}
          <ArrowRightIcon size={16} aria-hidden />
        </Link>
      </div>
      {children}
    </section>
  );
}

function FeaturedJobsError({ message, viewAllHref }: { message: string; viewAllHref: string }) {
  return (
    <div className={cn(cardClass, "p-6")}>
      <p className="text-sm text-ink-secondary">{message}</p>
      <Link href={viewAllHref} className={cn(buttonClass({ variant: "link" }), "mt-4 inline-flex")}>
        Try again
        <ArrowRightIcon size={16} aria-hidden />
      </Link>
    </div>
  );
}

async function GuestFeaturedJobs() {
  return (
    <GuestOrCandidateFeaturedJobs
      matchHint="sign_in"
      jobBasePath="/jobs"
      viewAllHref="/jobs"
      title="Recent jobs"
    />
  );
}

async function GuestOrCandidateFeaturedJobs({
  matchHint,
  jobBasePath,
  viewAllHref,
  title,
  applications,
}: {
  matchHint: "sign_in" | "none";
  jobBasePath: string;
  viewAllHref: string;
  title: string;
  applications?: { jobId: string }[];
}) {
  const result = await fetchPublicJobsResult(1, 3);

  if (!result.ok) {
    return (
      <FeaturedJobsShell title={title} viewAllHref={viewAllHref}>
        <FeaturedJobsError message="Jobs could not be loaded right now." viewAllHref={viewAllHref} />
      </FeaturedJobsShell>
    );
  }

  if (!result.items.length) {
    return (
      <FeaturedJobsShell title={title} viewAllHref={viewAllHref}>
        <p className="rounded-[var(--radius-card)] border bg-surface px-5 py-8 text-sm text-ink-secondary">
          No jobs are listed yet. Employers can publish roles after their account is approved.
        </p>
      </FeaturedJobsShell>
    );
  }

  return (
    <FeaturedJobsShell title={title} viewAllHref={viewAllHref}>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {result.items.map((job) => (
          <JobCard
            key={job.id}
            job={job}
            href={`${jobBasePath}/${job.id}`}
            matchHint={matchHint}
            applied={applications ? hasAppliedToJob(applications, job.id) : false}
          />
        ))}
      </div>
    </FeaturedJobsShell>
  );
}

async function CandidateFeaturedJobs() {
  const [jobsResult, applicationsResult] = await Promise.all([
    fetchPublicJobsResult(1, 3),
    fetchCandidateApplications(),
  ]);

  if (!jobsResult.ok) {
    return (
      <FeaturedJobsShell title="Open roles" viewAllHref="/candidate/jobs">
        <FeaturedJobsError message="Jobs could not be loaded right now." viewAllHref="/candidate/jobs" />
      </FeaturedJobsShell>
    );
  }

  if (!jobsResult.items.length) {
    return (
      <FeaturedJobsShell title="Open roles" viewAllHref="/candidate/jobs">
        <p className="rounded-[var(--radius-card)] border bg-surface px-5 py-8 text-sm text-ink-secondary">
          No open roles right now.{" "}
          <Link href="/candidate/jobs" className="font-semibold text-primary">
            Browse all jobs
          </Link>
        </p>
      </FeaturedJobsShell>
    );
  }

  const applications = applicationsResult.ok ? applicationsResult.items : [];

  return (
    <FeaturedJobsShell title="Open roles" viewAllHref="/candidate/jobs">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {jobsResult.items.map((job) => (
          <JobCard
            key={job.id}
            job={job}
            href={`/candidate/jobs/${job.id}`}
            matchHint="none"
            applied={hasAppliedToJob(applications, job.id)}
          />
        ))}
      </div>
    </FeaturedJobsShell>
  );
}

async function EmployerFeaturedJobs() {
  const result = await fetchEmployerJobs();

  if (!result.ok) {
    return (
      <FeaturedJobsShell title="Your open jobs" viewAllHref="/employer/jobs">
        <FeaturedJobsError message="Your jobs could not be loaded right now." viewAllHref="/employer/jobs" />
      </FeaturedJobsShell>
    );
  }

  const openJobs = result.items.filter((job) => job.status === "ACTIVE").slice(0, 3);

  if (!openJobs.length) {
    return (
      <FeaturedJobsShell title="Your open jobs" viewAllHref="/employer/jobs" viewAllLabel="Manage jobs">
        <p className="rounded-[var(--radius-card)] border bg-surface px-5 py-8 text-sm text-ink-secondary">
          No open jobs yet.{" "}
          <Link href="/employer/jobs/create" className="font-semibold text-primary">
            Create a job
          </Link>
        </p>
      </FeaturedJobsShell>
    );
  }

  return (
    <FeaturedJobsShell title="Your open jobs" viewAllHref="/employer/jobs" viewAllLabel="Manage jobs">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {openJobs.map((job) => (
          <EmployerHomeJobCard key={job.id} job={job} />
        ))}
      </div>
    </FeaturedJobsShell>
  );
}

export async function HomeFeaturedJobsSection() {
  const session = await getSession();

  if (!session) {
    return <GuestFeaturedJobs />;
  }

  if (session.role === "ADMIN") {
    return null;
  }

  if (session.role === "CANDIDATE") {
    return <CandidateFeaturedJobs />;
  }

  if (session.role === "EMPLOYER") {
    if (session.accountStatus !== "ACTIVE") {
      return null;
    }
    return <EmployerFeaturedJobs />;
  }

  return <GuestFeaturedJobs />;
}
