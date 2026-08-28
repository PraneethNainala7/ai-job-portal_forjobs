import {
  ArrowRightIcon,
  BriefcaseIcon,
  BuildingsIcon,
  MagnifyingGlassIcon,
} from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { AiPanel } from "@/components/ai/ai-panel";
import { PublicFooter, PublicHeader } from "@/components/layout/public-chrome";
import { buttonClass } from "@/components/ui/button";
import { cardInteractiveClass, controlClass } from "@/components/ui/control-styles";
import { JobCard } from "@/features/jobs/components/job-card";
import { fetchPublicJobs } from "@/lib/api/server";
import { cn } from "@/lib/utils";

export const dynamic = "force-dynamic";

const actions = [
  {
    title: "Find a job",
    body: "Create a candidate account, upload a resume, and review AI match insights before you apply.",
    href: "/register?role=CANDIDATE",
    cta: "Create candidate account",
    icon: BriefcaseIcon,
  },
  {
    title: "Hire talent",
    body: "Register your company with a CIN. After admin approval you can publish jobs and review applicants.",
    href: "/register?role=EMPLOYER",
    cta: "Create employer account",
    icon: BuildingsIcon,
  },
];

export default async function Home() {
  const featured = await fetchPublicJobs(1, 3);

  return (
    <div className="flex min-h-[100dvh] flex-col bg-canvas">
      <PublicHeader />
      <section className="mx-auto grid w-full max-w-[1400px] items-center gap-10 px-4 py-10 md:grid-cols-2 md:px-6 md:py-16 lg:px-8">
        <div>
          <h1 className="max-w-[16ch] text-4xl font-bold leading-[1.1] text-ink md:text-5xl">
            Hiring support that stays explainable.
          </h1>
          <p className="mt-5 max-w-[52ch] text-base leading-7 text-ink-secondary">
            Search roles, compare match insights, and manage applications. AI is decision support, not a hiring decision.
          </p>
          <form action="/jobs" className="mt-8 grid gap-3 sm:grid-cols-[1fr_auto]">
            <label className="sr-only" htmlFor="home-search">
              Search jobs
            </label>
            <div className="relative">
              <MagnifyingGlassIcon
                size={18}
                aria-hidden
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted"
              />
              <input
                id="home-search"
                name="search"
                type="search"
                placeholder="Role, skill, or location"
                className={cn(controlClass, "pl-10")}
              />
            </div>
            <button type="submit" className={buttonClass({ className: "px-5" })}>
              Find jobs
            </button>
          </form>
        </div>
        <AiPanel label="AI insight">
          <p className="mt-3 text-sm leading-6 text-ink-secondary">
            Match scores, strengths, and skill gaps help people review fit. Final shortlist and reject actions stay with the employer.
          </p>
        </AiPanel>
      </section>
      <section className="border-t bg-surface">
        <div className="mx-auto grid max-w-[1400px] gap-4 px-4 py-10 md:grid-cols-2 md:px-6 lg:px-8">
          {actions.map((item) => {
            const Icon = item.icon;
            return (
              <article key={item.title} className={cn(cardInteractiveClass, "p-6")}>
                <span className="grid size-10 place-items-center rounded-[var(--radius-control)] bg-primary-light text-primary">
                  <Icon size={20} weight="bold" aria-hidden />
                </span>
                <h2 className="mt-4 text-xl font-semibold">{item.title}</h2>
                <p className="mt-3 text-sm leading-6 text-ink-secondary">{item.body}</p>
                <Link href={item.href} className={cn(buttonClass({ variant: "link" }), "mt-6")}>
                  {item.cta}
                  <ArrowRightIcon size={16} aria-hidden />
                </Link>
              </article>
            );
          })}
        </div>
      </section>
      <section className="mx-auto w-full max-w-[1400px] px-4 py-10 md:px-6 lg:px-8">
        <div className="mb-6 flex items-end justify-between gap-4">
          <h2 className="text-2xl font-bold">Recent jobs</h2>
          <Link href="/jobs" className={cn(buttonClass({ variant: "link" }), "text-sm")}>
            View all
            <ArrowRightIcon size={16} aria-hidden />
          </Link>
        </div>
        {featured.length ? (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {featured.map((job) => (
              <JobCard key={job.id} job={job} matchHint="sign_in" />
            ))}
          </div>
        ) : (
          <p className="rounded-[var(--radius-card)] border bg-surface px-5 py-8 text-sm text-ink-secondary">
            No jobs are listed yet. Employers can publish roles after their account is approved.
          </p>
        )}
      </section>
      <PublicFooter />
    </div>
  );
}
