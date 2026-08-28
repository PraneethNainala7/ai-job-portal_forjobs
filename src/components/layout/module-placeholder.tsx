import { ArrowLeftIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";

interface ModulePlaceholderProps {
  area: string;
  description: string;
  nextModule: string;
}

export function ModulePlaceholder({
  area,
  description,
  nextModule,
}: ModulePlaceholderProps) {
  return (
    <section className="rounded-[var(--radius-card)] border bg-surface p-6">
      <p className="text-sm font-semibold text-primary">{area}</p>
      <p className="mt-3 max-w-[58ch] text-sm leading-6 text-ink-secondary">{description}</p>
      <p className="mt-5 text-sm text-ink-secondary">
        Continues in <strong className="font-semibold text-ink">{nextModule}</strong>.
      </p>
      {/* <Link
        href="/"
        className="mt-6 inline-flex h-11 items-center gap-2 rounded-[var(--radius-control)] border px-4 text-sm font-semibold"
      >
        <ArrowLeftIcon size={18} />
        Back home
      </Link> */}
    </section>
  );
}
