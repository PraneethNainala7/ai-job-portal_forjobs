import { MagnifyingGlassIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { AiPanel } from "@/components/ai/ai-panel";
import { buttonClass } from "@/components/ui/button";
import { controlClass } from "@/components/ui/control-styles";
import { getHomeHeroContent } from "@/features/home/home-hero";
import { getSession } from "@/lib/auth/session";
import { cn } from "@/lib/utils";

export async function HomeHeroSection() {
  const session = await getSession();
  const hero = getHomeHeroContent(session);
  const showInsight = session?.role === "CANDIDATE";

  return (
    <section
      className={cn(
        "mx-auto grid w-full max-w-[1400px] items-center gap-10 px-4 py-10 md:px-6 md:py-16 lg:px-8",
        showInsight && "md:grid-cols-2",
      )}
    >
      <div>
        <h1 className="max-w-[16ch] text-4xl font-bold leading-[1.1] text-ink md:text-5xl">{hero.headline}</h1>
        <p className="mt-5 max-w-[52ch] text-base leading-7 text-ink-secondary">{hero.description}</p>
        {hero.primaryAction.type === "search" ? (
          <form action={hero.primaryAction.action} className="mt-8 grid gap-3 sm:grid-cols-[1fr_auto]">
            <label className="sr-only" htmlFor="home-search">
              Search jobs
            </label>
            <div className="relative">
              <MagnifyingGlassIcon
                size={18}
                aria-hidden
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-muted"
              />
              <input
                id="home-search"
                name="search"
                type="search"
                placeholder={hero.primaryAction.placeholder}
                className={cn(controlClass, "pl-10")}
              />
            </div>
            <button type="submit" className={buttonClass({ className: "px-5" })}>
              {hero.primaryAction.buttonLabel}
            </button>
          </form>
        ) : (
          <Link href={hero.primaryAction.href} className={buttonClass({ className: "mt-8 inline-flex px-5" })}>
            {hero.primaryAction.label}
          </Link>
        )}
      </div>
      {showInsight ? (
        <AiPanel label={hero.insightLabel}>
          {hero.insightHref ? (
            <Link
              href={hero.insightHref}
              aria-label="Go to AI recommendations"
              className="mt-3 block text-sm leading-6 text-ink-secondary underline-offset-2 transition-colors hover:text-primary hover:underline"
            >
              {hero.insightBody}
            </Link>
          ) : (
            <p className="mt-3 text-sm leading-6 text-ink-secondary">{hero.insightBody}</p>
          )}
        </AiPanel>
      ) : null}
    </section>
  );
}
