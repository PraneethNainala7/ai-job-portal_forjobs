import { getHomePath } from "@/config/routes";
import type { SessionUser } from "@/types/domain";

export type HomeHeroSearchAction = {
  type: "search";
  action: string;
  placeholder: string;
  buttonLabel: string;
};

export type HomeHeroLinkAction = {
  type: "link";
  href: string;
  label: string;
};

export type HomeHeroPrimaryAction = HomeHeroSearchAction | HomeHeroLinkAction;

export type HomeHeroContent = {
  headline: string;
  description: string;
  insightLabel: string;
  insightBody: string;
  insightHref?: string;
  primaryAction: HomeHeroPrimaryAction;
};

const guestHero: HomeHeroContent = {
  headline: "Hiring support that stays explainable.",
  description:
    "Search roles, compare match insights, and manage applications. AI is decision support, not a hiring decision.",
  insightLabel: "AI insight",
  insightBody:
    "Match scores, strengths, and skill gaps help people review fit. Final shortlist and reject actions stay with the employer.",
  primaryAction: {
    type: "search",
    action: "/jobs",
    placeholder: "Role, skill, or location",
    buttonLabel: "Find jobs",
  },
};

function candidateHero(session: SessionUser): HomeHeroContent {
  if (session.accountStatus === "ACTIVE") {
    return {
      headline: "Find roles that fit your profile.",
      description:
        "Browse every open role in Find jobs with match scores on each card. AI recommendations surface only strong matches at 60% or higher.",
      insightLabel: "AI insight",
      insightBody:
        "Match scores and skill gaps help you decide where to apply. Recommendations are a curated shortlist — they do not apply for you.",
      insightHref: "/candidate/recommendations",
      primaryAction: {
        type: "search",
        action: "/candidate/jobs",
        placeholder: "Role, skill, or location",
        buttonLabel: "Browse roles",
      },
    };
  }

  return {
    headline: "Your candidate workspace is paused.",
    description:
      "Protected features such as applying and AI recommendations are unavailable until your account is active again.",
    insightLabel: "Account status",
    insightBody: "Your profile and application history are preserved. Review your status for next steps.",
    primaryAction: {
      type: "link",
      href: getHomePath(session.role, session.accountStatus),
      label: "View account status",
    },
  };
}

function employerHero(session: SessionUser): HomeHeroContent {
  if (session.accountStatus === "ACTIVE") {
    return {
      headline: "Hire with explainable match support.",
      description:
        "Post jobs, review applicants with match insights, and shortlist or reject on your terms. AI supports review — it does not hire for you.",
      insightLabel: "AI insight",
      insightBody:
        "Applicant match scores highlight overlap and gaps. Shortlist and reject decisions always stay with your team.",
      primaryAction: {
        type: "link",
        href: "/employer/jobs/create",
        label: "Post a job",
      },
    };
  }

  if (session.accountStatus === "PENDING") {
    return {
      headline: "Registration under review.",
      description:
        "Your company registration is pending admin approval. You can update your profile, but job posting stays locked until approval.",
      insightLabel: "Approval status",
      insightBody: "We will email you when a decision is made. Confirm your CIN and company details are complete.",
      primaryAction: {
        type: "link",
        href: "/employer/pending",
        label: "Check approval status",
      },
    };
  }

  if (session.accountStatus === "REJECTED") {
    return {
      headline: "Update your company registration.",
      description:
        "Your employer registration was not approved. Review the reason, update your company profile, and resubmit for review.",
      insightLabel: "Next steps",
      insightBody: "Fix the issues noted in your rejection, then resubmit from your company profile.",
      primaryAction: {
        type: "link",
        href: "/employer/profile",
        label: "Update company profile",
      },
    };
  }

  return {
    headline: "Employer access is restricted.",
    description: "Hiring features are temporarily unavailable. Existing jobs and applicant data are preserved.",
    insightLabel: "Account status",
    insightBody: "Contact the platform administrator if you believe this hold was applied in error.",
    primaryAction: {
      type: "link",
      href: getHomePath(session.role, session.accountStatus),
      label: "View account status",
    },
  };
}

const adminHero: HomeHeroContent = {
  headline: "Platform oversight, one place.",
  description: "Monitor users, jobs, applications, and pending employer approvals across the portal.",
  insightLabel: "Operations",
  insightBody:
    "AI match usage supports hiring workflows on the platform. Admin actions focus on accounts, approvals, and audit visibility.",
  primaryAction: {
    type: "link",
    href: "/admin/dashboard",
    label: "Open admin dashboard",
  },
};

export function getHomeHeroContent(session: SessionUser | null): HomeHeroContent {
  if (!session) return guestHero;

  switch (session.role) {
    case "CANDIDATE":
      return candidateHero(session);
    case "EMPLOYER":
      return employerHero(session);
    case "ADMIN":
      return adminHero;
    default:
      return guestHero;
  }
}
