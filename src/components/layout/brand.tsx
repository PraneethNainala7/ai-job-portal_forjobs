import { BriefcaseIcon } from "@phosphor-icons/react/dist/ssr";
import Link from "next/link";
import { cn } from "@/lib/utils";

export function Brand({ inverted = false }: { inverted?: boolean }) {
  return (
    <Link
      href="/"
      className={cn(
        "flex min-h-11 items-center gap-3 rounded-[var(--radius-control)]",
        inverted ? "text-white" : "text-ink",
      )}
    >
      <span
        className={cn(
          "grid size-9 place-items-center rounded-[var(--radius-control)]",
          inverted ? "bg-primary text-white" : "bg-brand text-white",
        )}
      >
        <BriefcaseIcon size={18} weight="fill" />
      </span>
      <span className="text-sm font-bold tracking-tight">AI Job Portal</span>
    </Link>
  );
}
