import type { AccountStatus, UserRole } from "@/types/domain";

export function getHomePath(role: UserRole, status: AccountStatus) {
  if (role === "ADMIN") return "/admin/dashboard";
  if (role === "CANDIDATE") {
    return status === "ACTIVE" ? "/candidate/dashboard" : "/candidate/restricted";
  }
  if (status === "PENDING") return "/employer/pending";
  if (status === "REJECTED") return "/employer/rejected";
  if (status === "ACTIVE") return "/employer/dashboard";
  return "/employer/restricted";
}
