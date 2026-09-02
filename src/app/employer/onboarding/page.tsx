import { redirect } from "next/navigation";
import { employerRegisterStep2Path } from "@/config/routes";

export default function EmployerOnboardingPage() {
  redirect(employerRegisterStep2Path);
}
