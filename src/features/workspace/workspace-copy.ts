import type { SessionUser } from "@/types/domain";

export type DashboardCopy = {
  description: string;
};

export function getDashboardCopy(session: SessionUser): DashboardCopy {
  switch (session.role) {
    case "CANDIDATE":
      return {
        description:
          "Keep your resume current, browse all open roles in Find jobs with scores on every card, then check AI recommendations for strong matches before you apply.",
      };
    case "EMPLOYER":
      return {
        description: `${session.companyName ?? "Your company"}: track jobs, applicants, and shortlists. Match scores are decision support — final hiring actions stay with you.`,
      };
    case "ADMIN":
      return {
        description:
          "Monitor users, jobs, and applications across the portal. Pending employer approvals and audit logs are your primary operational actions.",
      };
    default:
      return {
        description: "Review your workspace and continue where you left off.",
      };
  }
}
