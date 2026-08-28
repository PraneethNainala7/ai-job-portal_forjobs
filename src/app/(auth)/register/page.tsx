import Link from "next/link";
import { Suspense } from "react";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { AuthSplit } from "@/features/auth/components/auth-split";
import { RegisterForm } from "@/features/auth/components/register-form";

export default function RegisterPage() {
  return (
    <AuthSplit
      title="Create your account"
      description="Pick how you want to use the portal, then add a few details."
      footer={
        <p className="mt-4 text-sm text-ink-secondary">
          Already have an account?{" "}
          <Link href="/login" className="font-semibold text-primary">
            Login
          </Link>
        </p>
      }
    >
      <Suspense fallback={<LoadingPanel height="sm" message="Loading form..." className="border-0 bg-transparent shadow-none" />}>
        <RegisterForm />
      </Suspense>
    </AuthSplit>
  );
}
