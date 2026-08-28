import Link from "next/link";
import { Brand } from "@/components/layout/brand";
import { cardClass } from "@/components/ui/control-styles";
import { LoginForm } from "@/features/auth/components/login-form";
import { cn } from "@/lib/utils";

export default function LoginPage() {
  return (
    <div className="flex h-full flex-col items-center justify-center overflow-y-auto px-4 py-10">
      <section className={cn(cardClass, "w-full max-w-md p-6")}>
        <div className="mb-6 flex justify-center">
          <Brand />
        </div>
        <h1 className="text-2xl font-bold">Welcome back</h1>
        <p className="mt-2 text-sm text-ink-secondary">Sign in to continue to your workspace.</p>
        <div className="mt-6">
          <LoginForm />
        </div>
        <p className="mt-5 text-sm text-ink-secondary">
          New here? <Link href="/register" className="font-semibold text-primary">Register</Link>
        </p>
      </section>
      {/* <p className="mt-8 text-sm text-ink-secondary">
        <Link href="/" className="font-semibold text-primary">
          Back home
        </Link>
      </p> */}
    </div>
  );
}
