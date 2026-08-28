import { RoleGate } from "@/components/layout/role-gate";

export default function CandidateLayout({ children }: { children: React.ReactNode }) {
  return <RoleGate role="CANDIDATE">{children}</RoleGate>;
}
