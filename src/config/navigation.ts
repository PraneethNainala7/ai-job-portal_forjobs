import type { Icon } from "@phosphor-icons/react";
import {
  BriefcaseIcon,
  BuildingsIcon,
  ChartBarIcon,
  ClipboardTextIcon,
  FileTextIcon,
  GaugeIcon,
  IdentificationCardIcon,
  MagnifyingGlassIcon,
  SparkleIcon,
  UsersThreeIcon,
} from "@phosphor-icons/react";
import type { AccountStatus, UserRole } from "@/types/domain";

export interface NavItem {
  label: string;
  href: string;
  icon: Icon;
}

export function getRoleNavigation(role: UserRole, status: AccountStatus): NavItem[] {
  if (role === "CANDIDATE") {
    if (status === "ON_HOLD" || status === "INACTIVE") {
      return [{ label: "Account status", href: "/candidate/restricted", icon: IdentificationCardIcon }];
    }
    return [
      { label: "Dashboard", href: "/candidate/dashboard", icon: GaugeIcon },
      { label: "Find jobs", href: "/candidate/jobs", icon: MagnifyingGlassIcon },
      { label: "AI recommendations", href: "/candidate/recommendations", icon: SparkleIcon },
      { label: "My profile", href: "/candidate/profile", icon: IdentificationCardIcon },
      { label: "Resume", href: "/candidate/resume", icon: FileTextIcon },
      { label: "My applications", href: "/candidate/applications", icon: BriefcaseIcon },
    ];
  }

  if (role === "EMPLOYER") {
    if (status === "PENDING") {
      return [
        { label: "Approval status", href: "/employer/pending", icon: ClipboardTextIcon },
        { label: "Company profile", href: "/employer/profile", icon: BuildingsIcon },
      ];
    }
    if (status === "REJECTED") {
      return [
        { label: "Approval status", href: "/employer/rejected", icon: ClipboardTextIcon },
        { label: "Update company", href: "/employer/profile", icon: BuildingsIcon },
      ];
    }
    if (status === "ON_HOLD" || status === "INACTIVE") {
      return [{ label: "Account status", href: "/employer/restricted", icon: BuildingsIcon }];
    }
    return [
      { label: "Dashboard", href: "/employer/dashboard", icon: GaugeIcon },
      { label: "Manage jobs", href: "/employer/jobs", icon: BriefcaseIcon },
      { label: "Applicants", href: "/employer/applicants", icon: UsersThreeIcon },
      { label: "Company profile", href: "/employer/profile", icon: BuildingsIcon },
    ];
  }

  return [
    { label: "Dashboard", href: "/admin/dashboard", icon: GaugeIcon },
    { label: "Employer approvals", href: "/admin/approvals", icon: ClipboardTextIcon },
    { label: "Candidates", href: "/admin/candidates", icon: UsersThreeIcon },
    { label: "Employers", href: "/admin/employers", icon: BuildingsIcon },
    { label: "Jobs", href: "/admin/jobs", icon: BriefcaseIcon },
    { label: "Applications", href: "/admin/applications", icon: FileTextIcon },
    { label: "Audit activity", href: "/admin/audit", icon: ChartBarIcon },
  ];
}

