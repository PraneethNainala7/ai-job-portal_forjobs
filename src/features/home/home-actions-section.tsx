import { ArrowRightIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { cardInteractiveClass } from "@/components/ui/control-styles";
import { getHomeActions } from "@/features/home/home-actions";
import { getSession } from "@/lib/auth/session";
import { cn } from "@/lib/utils";

export async function HomeActionsSection() {
  const session = await getSession();
  const actions = getHomeActions(session);

  return (
    <section className="border-t bg-surface">
      <div className="mx-auto grid max-w-[1400px] gap-4 px-4 py-10 md:grid-cols-2 md:px-6 lg:px-8">
        {actions.map((item) => {
          const Icon = item.icon;
          return (
            <Link
              key={item.title}
              href={item.href}
              className={cn(cardInteractiveClass, "block p-6 no-underline")}
            >
              <article>
                <span className="grid size-10 place-items-center rounded-[var(--radius-control)] bg-primary-light text-primary">
                  <Icon size={20} weight="bold" aria-hidden />
                </span>
                <h2 className="mt-4 text-xl font-semibold text-ink">{item.title}</h2>
                <p className="mt-3 text-sm leading-6 text-ink-secondary">{item.body}</p>
                <p className="mt-6 inline-flex items-center gap-2 text-sm font-semibold text-primary">
                  {item.cta}
                  <ArrowRightIcon size={16} aria-hidden />
                </p>
              </article>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
