import { RoleGate } from "@/components/layout/role-gate";

export default function EmployerLayout({ children }: { children: React.ReactNode }) {
  return <RoleGate role="EMPLOYER">{children}</RoleGate>;
}
