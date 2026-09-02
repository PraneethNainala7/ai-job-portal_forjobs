import Link from "next/link";
import { Suspense } from "react";
import { Brand } from "@/components/layout/brand";
import { cardClass } from "@/components/ui/control-styles";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { LoginForm } from "@/features/auth/components/login-form";
import { cn } from "@/lib/utils";

type LoginPageProps = {
  searchParams: Promise<{ role?: string }>;
};

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const params = await searchParams;
  const employerLogin = params.role === "EMPLOYER";

  return (
    <div className="flex h-full flex-col items-center justify-center overflow-y-auto px-4 py-10">
      <section className={cn(cardClass, "w-full max-w-md p-6")}>
        <div className="mb-6 flex justify-center">
          <Brand />
        </div>
        <h1 className="text-2xl font-bold">Welcome back</h1>
        <p className="mt-2 text-sm text-ink-secondary">
          {employerLogin
            ? "Sign in with your company email and password."
            : "Sign in to continue to your workspace."}
        </p>
        <div className="mt-6">
          <Suspense fallback={<LoadingPanel height="sm" message="Loading form..." className="border-0 bg-transparent shadow-none" />}>
            <LoginForm />
          </Suspense>
        </div>
        <p className="mt-5 text-sm text-ink-secondary">
          New here?{" "}
          <Link
            href={employerLogin ? "/register?role=EMPLOYER" : "/register"}
            className="font-semibold text-primary"
          >
            Register
          </Link>
        </p>
      </section>
    </div>
  );
}
