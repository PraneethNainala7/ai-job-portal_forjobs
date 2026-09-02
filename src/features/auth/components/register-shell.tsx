"use client";

import Link from "next/link";
import { Suspense, useCallback, useEffect, useState } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { AuthSplit } from "@/features/auth/components/auth-split";
import { RegisterForm } from "@/features/auth/components/register-form";
import {
  getEmployerCompanyStepCopy,
  getRegisterCopy,
  parseRegisterRole,
  type RegisterRole,
} from "@/features/auth/register-copy";

function registerPath(role: RegisterRole) {
  return role === "EMPLOYER" ? "/register?role=EMPLOYER" : "/register?role=CANDIDATE";
}

function roleFromLocationSearch() {
  return parseRegisterRole(new URLSearchParams(window.location.search).get("role") ?? undefined);
}

export function RegisterShell({
  initialRole,
  isEmployerCompanyStep,
}: {
  initialRole: RegisterRole;
  isEmployerCompanyStep: boolean;
}) {
  const [displayRole, setDisplayRole] = useState(initialRole);

  useEffect(() => {
    setDisplayRole(initialRole);
  }, [initialRole]);

  const handleRoleChange = useCallback((role: RegisterRole) => {
    setDisplayRole(role);
    window.history.replaceState(null, "", registerPath(role));
  }, []);

  useEffect(() => {
    if (isEmployerCompanyStep) return;

    function onPopState() {
      setDisplayRole(roleFromLocationSearch());
    }

    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, [isEmployerCompanyStep]);

  const copy = isEmployerCompanyStep ? getEmployerCompanyStepCopy() : getRegisterCopy(displayRole);
  const employer = displayRole === "EMPLOYER";

  return (
    <AuthSplit
      title={copy.title}
      description={copy.description}
      asideHeadline={copy.asideHeadline}
      asideBody={copy.asideBody}
      asideFootnote={copy.asideFootnote}
      footer={
        isEmployerCompanyStep ? null : (
          <p className="mt-3 text-sm text-ink-secondary">
            Already have an account?{" "}
            <Link href={employer ? "/login?role=EMPLOYER" : "/login"} className="font-semibold text-primary">
              Login
            </Link>
          </p>
        )
      }
    >
      <Suspense
        fallback={
          <LoadingPanel height="sm" message="Loading form..." className="border-0 bg-transparent shadow-none" />
        }
      >
        <RegisterForm displayRole={displayRole} onRoleChange={handleRoleChange} />
      </Suspense>
    </AuthSplit>
  );
}
