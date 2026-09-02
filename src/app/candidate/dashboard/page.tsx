import {
  BriefcaseIcon,
  ClipboardTextIcon,
  FileTextIcon,
  UserCircleIcon,
} from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { PageHeader } from "@/components/layout/page-header";
import { cardInteractiveClass } from "@/components/ui/control-styles";
import { ApplicationList } from "@/features/candidate/components/application-list";
import { RecommendationList } from "@/features/candidate/components/recommendation-list";
import { getDashboardCopy } from "@/features/workspace/workspace-copy";
import { getFreshSession } from "@/lib/auth/session";
import { redirect } from "next/navigation";
import { cn } from "@/lib/utils";

const links = [
  { href: "/candidate/profile", label: "My profile", icon: UserCircleIcon },
  { href: "/candidate/resume", label: "Resume", icon: FileTextIcon },
  { href: "/candidate/jobs", label: "Find jobs", icon: BriefcaseIcon },
  { href: "/candidate/applications", label: "Applications", icon: ClipboardTextIcon },
];

export default async function CandidateDashboardPage() {
  const session = await getFreshSession();
  if (!session) redirect("/login");

  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate" }, { label: "Dashboard" }]}
        title={`Welcome, ${session.name}`}
        description={getDashboardCopy(session).description}
      />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {links.map((link) => {
          const Icon = link.icon;
          return (
            <Link key={link.href} href={link.href} className={cn(cardInteractiveClass, "flex items-center gap-3 p-5 text-sm font-semibold")}>
              <span className="grid size-9 place-items-center rounded-[var(--radius-control)] bg-muted text-ink-secondary">
                <Icon size={18} aria-hidden />
              </span>
              {link.label}
            </Link>
          );
        })}
      </div>
      <section className="mt-8">
        <h2 className="mb-4 text-xl font-semibold">AI recommendations</h2>
        <RecommendationList />
      </section>
      <section className="mt-8">
        <h2 className="mb-4 text-xl font-semibold">Recent applications</h2>
        <ApplicationList />
      </section>
    </>
  );
}
