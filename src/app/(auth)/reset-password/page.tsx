import Link from "next/link";
import { Suspense } from "react";
import { Brand } from "@/components/layout/brand";
import { cardClass } from "@/components/ui/control-styles";
import { LoadingPanel } from "@/components/ui/loading-panel";
import { ResetPasswordForm } from "@/features/auth/components/reset-password-form";
import { cn } from "@/lib/utils";

export default function ResetPasswordPage() {
  return (
    <div className="flex h-full flex-col items-center justify-center overflow-y-auto px-4 py-10">
      <section className={cn(cardClass, "w-full max-w-md p-6")}>
        <div className="mb-6 flex justify-center">
          <Brand />
        </div>
        <h1 className="text-2xl font-bold">Choose a new password</h1>
        <p className="mt-2 text-sm text-ink-secondary">
          Enter and confirm your new password below.
        </p>
        <div className="mt-6">
          <Suspense
            fallback={
              <LoadingPanel
                height="sm"
                message="Loading form..."
                className="border-0 bg-transparent shadow-none"
              />
            }
          >
            <ResetPasswordForm />
          </Suspense>
        </div>
        <p className="mt-5 text-sm text-ink-secondary">
          <Link href="/login" className="font-semibold text-primary">
            Back to login
          </Link>
        </p>
      </section>
    </div>
  );
}
