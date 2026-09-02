import { RegisterShell } from "@/features/auth/components/register-shell";

import { parseRegisterRole } from "@/features/auth/register-copy";



type RegisterPageProps = {

  searchParams: Promise<{ role?: string; step?: string }>;

};



export default async function RegisterPage({ searchParams }: RegisterPageProps) {

  const params = await searchParams;

  const role = parseRegisterRole(params.role);

  const isEmployerCompanyStep = params.step === "2" && role === "EMPLOYER";



  return <RegisterShell initialRole={role} isEmployerCompanyStep={isEmployerCompanyStep} />;

}


