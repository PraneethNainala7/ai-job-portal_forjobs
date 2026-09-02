import type { Icon } from "@phosphor-icons/react";
import {
  BriefcaseIcon,
  BuildingsIcon,
  ClipboardTextIcon,
  GaugeIcon,
  ShieldCheckIcon,
  UserCircleIcon,
  UsersThreeIcon,
  WarningCircleIcon,
} from "@phosphor-icons/react/dist/ssr";
import { getHomePath } from "@/config/routes";
import type { SessionUser } from "@/types/domain";

export type HomeAction = {
  title: string;
  body: string;
  href: string;
  cta: string;
  icon: Icon;
};

const guestActions: HomeAction[] = [
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

function candidateActions(session: SessionUser): HomeAction[] {
  if (session.accountStatus === "ACTIVE") {
    return [
      {
        title: "Find jobs",
        body: "Browse open roles with filters and review AI match insights before you apply.",
        href: "/candidate/jobs",
        cta: "Browse roles",
        icon: BriefcaseIcon,
      },
      {
        title: "Your applications",
        body: "Track statuses, view applied roles, and follow up on roles you have already submitted.",
        href: "/candidate/applications",
        cta: "View applications",
        icon: ClipboardTextIcon,
      },
    ];
  }

  return [
    {
      title: "Account status",
      body: "Review your current account state and what is needed before protected hiring features unlock.",
      href: getHomePath(session.role, session.accountStatus),
      cta: "View status",
      icon: WarningCircleIcon,
    },
    {
      title: "Complete profile",
      body: "Update your candidate profile so it is ready when your account becomes active.",
      href: "/candidate/profile",
      cta: "Open profile",
      icon: UserCircleIcon,
    },
  ];
}

function employerActions(session: SessionUser): HomeAction[] {
  if (session.accountStatus === "ACTIVE") {
    return [
      {
        title: "Post a job",
        body: "Publish a new role and start receiving applicants with AI match support for review.",
        href: "/employer/jobs/create",
        cta: "Create job",
        icon: BriefcaseIcon,
      },
      {
        title: "Review applicants",
        body: "See who applied across your jobs, shortlist candidates, and manage hiring progress.",
        href: "/employer/applicants",
        cta: "View applicants",
        icon: UsersThreeIcon,
      },
    ];
  }

  if (session.accountStatus === "PENDING") {
    return [
      {
        title: "Approval status",
        body: "Check where your company registration stands while admin review is in progress.",
        href: "/employer/pending",
        cta: "View status",
        icon: WarningCircleIcon,
      },
      {
        title: "Company profile",
        body: "Confirm your company details and CIN are complete for faster approval.",
        href: "/employer/profile",
        cta: "Open profile",
        icon: BuildingsIcon,
      },
    ];
  }

  if (session.accountStatus === "REJECTED") {
    return [
      {
        title: "Rejection details",
        body: "Read why the submission was rejected and what to fix before resubmitting.",
        href: "/employer/rejected",
        cta: "View details",
        icon: WarningCircleIcon,
      },
      {
        title: "Update and resubmit",
        body: "Revise your company profile and resubmit for admin approval.",
        href: "/employer/profile",
        cta: "Update profile",
        icon: BuildingsIcon,
      },
    ];
  }

  return [
    {
      title: "Account status",
      body: "Review your employer account state and next steps for restoring hiring access.",
      href: getHomePath(session.role, session.accountStatus),
      cta: "View status",
      icon: WarningCircleIcon,
    },
    {
      title: "Company profile",
      body: "Keep your company information current while your account status is resolved.",
      href: "/employer/profile",
      cta: "Open profile",
      icon: BuildingsIcon,
    },
  ];
}

const adminActions: HomeAction[] = [
  {
    title: "Platform overview",
    body: "Monitor users, jobs, and applications across the portal from one dashboard.",
    href: "/admin/dashboard",
    cta: "Open dashboard",
    icon: GaugeIcon,
  },
  {
    title: "Pending approvals",
    body: "Review employer registrations waiting for admin approval before they can publish jobs.",
    href: "/admin/approvals",
    cta: "Review approvals",
    icon: ShieldCheckIcon,
  },
];

export function getHomeActions(session: SessionUser | null): HomeAction[] {
  if (!session) return guestActions;

  switch (session.role) {
    case "CANDIDATE":
      return candidateActions(session);
    case "EMPLOYER":
      return employerActions(session);
    case "ADMIN":
      return adminActions;
    default:
      return guestActions;
  }
}
