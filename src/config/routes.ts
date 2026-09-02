import type { AccountStatus, UserRole } from "@/types/domain";



type HomePathOptions = {

  cin?: string;

  needsEmployerOnboarding?: boolean;

};



export const employerRegisterStep2Path = "/register?role=EMPLOYER&step=2";



export function getHomePath(role: UserRole, status: AccountStatus, options?: HomePathOptions) {

  if (role === "ADMIN") return "/admin/dashboard";

  if (role === "CANDIDATE") {

    return status === "ACTIVE" ? "/candidate/dashboard" : "/candidate/restricted";

  }

  if (options?.needsEmployerOnboarding || (status === "PENDING" && !options?.cin)) {

    return employerRegisterStep2Path;

  }

  if (status === "PENDING") return "/employer/pending";

  if (status === "REJECTED") return "/employer/rejected";

  if (status === "ACTIVE") return "/employer/dashboard";

  return "/employer/restricted";

}



function matchesPath(pathname: string, allowed: string) {

  return pathname === allowed || pathname.startsWith(`${allowed}/`);

}



export function isEmployerRouteAllowed(status: AccountStatus, pathname: string) {

  if (!pathname.startsWith("/employer")) return false;

  if (status === "ACTIVE") {
    if (matchesPath(pathname, "/employer/restricted")) return false;
    if (matchesPath(pathname, "/employer/pending")) return false;
    if (matchesPath(pathname, "/employer/rejected")) return false;
    return true;
  }

  if (status === "PENDING") {

    return matchesPath(pathname, "/employer/pending") || matchesPath(pathname, "/employer/profile");

  }

  if (status === "REJECTED") {

    return matchesPath(pathname, "/employer/rejected") || matchesPath(pathname, "/employer/profile");

  }

  if (status === "ON_HOLD" || status === "INACTIVE") {

    return matchesPath(pathname, "/employer/restricted");

  }

  return false;

}



export function isCandidateRouteAllowed(status: AccountStatus, pathname: string) {

  if (!pathname.startsWith("/candidate")) return false;

  if (status === "ACTIVE") {
    if (matchesPath(pathname, "/candidate/restricted")) return false;
    return true;
  }

  if (status === "ON_HOLD" || status === "INACTIVE") {

    return matchesPath(pathname, "/candidate/restricted");

  }

  return false;

}

