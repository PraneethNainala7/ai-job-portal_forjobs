import Link from "next/link";
import { Brand } from "@/components/layout/brand";
import { cardClass } from "@/components/ui/control-styles";
import { ForgotPasswordForm } from "@/features/auth/components/forgot-password-form";
import { cn } from "@/lib/utils";

export default function ForgotPasswordPage() {
  return (
    <div className="flex h-full flex-col items-center justify-center overflow-y-auto px-4 py-10">
      <section className={cn(cardClass, "w-full max-w-md p-6")}>
        <div className="mb-6 flex justify-center">
          <Brand />
        </div>
        <h1 className="text-2xl font-bold">Reset your password</h1>
        <p className="mt-2 text-sm text-ink-secondary">
          Enter your email and we&apos;ll send you a link to choose a new password.
        </p>
        <div className="mt-6">
          <ForgotPasswordForm />
        </div>
        <p className="mt-5 text-sm text-ink-secondary">
          Remember your password?{" "}
          <Link href="/login" className="font-semibold text-primary">
            Back to login
          </Link>
        </p>
      </section>
    </div>
  );
}
