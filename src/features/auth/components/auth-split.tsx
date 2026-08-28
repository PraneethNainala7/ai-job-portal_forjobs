import { Brand } from "@/components/layout/brand";
import { AuthGraphic } from "@/features/auth/components/auth-graphic";

export function AuthSplit({
  title,
  description,
  children,
  footer,
}: {
  title: string;
  description: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}) {
  return (
    <div className="grid h-dvh w-full grid-cols-1 overflow-hidden lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]">
      <aside className="relative hidden min-h-0 min-w-0 overflow-hidden bg-brand text-white lg:flex lg:flex-col">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_50%_32%,rgb(79_70_229_/_0.18),transparent_52%)]"
        />

        <div className="relative shrink-0 px-10 pt-8 xl:px-14 xl:pt-10">
          <Brand inverted />
        </div>

        <div className="relative min-h-0 min-w-0 flex-1 overflow-hidden px-6 py-4 xl:px-10">
          <AuthGraphic />
        </div>

        <div className="relative shrink-0 px-10 pb-8 xl:px-14 xl:pb-10">
          <h2 className="max-w-[16ch] text-2xl font-bold leading-[1.15] xl:text-3xl">
            Hiring decisions stay human.
          </h2>
          <p className="mt-3 max-w-[38ch] text-sm leading-6 text-white/70">
            The AI reads the resume and surfaces fit. Shortlist and reject stay with you.
          </p>
          <p className="mt-4 text-xs text-white/45">
            Match insights support decisions. They do not hire for you.
          </p>
        </div>
      </aside>

      <div className="flex min-h-0 min-w-0 flex-col overflow-hidden bg-surface">
        <div className="flex shrink-0 items-center px-5 py-5 sm:px-10 lg:hidden">
          <Brand />
        </div>
        <div className="flex min-h-0 flex-1 flex-col justify-center overflow-hidden px-5 pb-8 sm:px-10 xl:px-16">
          <div className="mx-auto w-full max-w-md">
            <h1 className="text-2xl font-bold">{title}</h1>
            <p className="mt-2 text-sm leading-6 text-ink-secondary">{description}</p>
            <div className="mt-7">{children}</div>
            {footer}
          </div>
        </div>
      </div>
    </div>
  );
}
