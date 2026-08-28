import { MagnifyingGlassIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { Brand } from "@/components/layout/brand";
import { buttonClass } from "@/components/ui/button";
import { getHomePath } from "@/config/routes";
import { getSession } from "@/lib/auth/session";
import { cn } from "@/lib/utils";

export async function PublicHeader() {
  const session = await getSession();
  const accountHref = session ? getHomePath(session.role, session.accountStatus) : "/login";

  return (
    <header className="sticky top-0 z-30 border-b bg-surface/95 backdrop-blur-md">
      <div className="mx-auto flex h-[72px] max-w-[1400px] items-center justify-between gap-3 px-4 md:px-6 lg:px-8">
        <Brand />
        <nav className="flex items-center gap-2 sm:gap-3" aria-label="Public navigation">
          <Link
            href="/jobs"
            className="hidden min-h-11 items-center gap-2 px-3 text-sm font-medium text-ink-secondary hover:text-ink sm:inline-flex"
          >
            <MagnifyingGlassIcon size={16} aria-hidden />
            Browse jobs
          </Link>
          {session ? (
            <Link href={accountHref} className={buttonClass()}>
              Workspace
            </Link>
          ) : (
            <>
              <Link href="/login" className={buttonClass({ variant: "secondary" })}>
                Login
              </Link>
              <Link href="/register" className={buttonClass()}>
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

export function PublicFooter() {
  return (
    <footer className="mt-auto border-t bg-surface">
      <div className="mx-auto flex max-w-[1400px] flex-col gap-2 px-4 py-6 text-sm text-ink-secondary md:flex-row md:items-center md:justify-between md:px-6 lg:px-8">
        <p>AI Job Portal. Match insights support decisions. They do not hire for you.</p>
        <Link href="/jobs" className={cn(buttonClass({ variant: "link" }), "justify-start font-medium")}>
          Browse jobs
        </Link>
      </div>
    </footer>
  );
}
