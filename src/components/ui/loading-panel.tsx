import { AnimeLoader } from "@/components/ui/anime-loader";
import { cardClass } from "@/components/ui/control-styles";
import { cn } from "@/lib/utils";

const heightClass = {
  sm: "min-h-40",
  md: "min-h-64",
  lg: "min-h-80",
} as const;

type LoadingHeight = keyof typeof heightClass;

function LoadingShell({
  message,
  variant = "default",
  height,
  className,
  loaderSize = "md",
  children,
}: {
  message: string;
  variant?: "default" | "ai";
  height?: LoadingHeight;
  className?: string;
  loaderSize?: "sm" | "md" | "lg";
  children?: React.ReactNode;
}) {
  const shellClass =
    variant === "ai"
      ? "rounded-[var(--radius-card)] border border-ai-border bg-ai-surface p-5 shadow-[var(--shadow-card)]"
      : cn(cardClass, "border-line");

  return (
    <div
      role="status"
      aria-live="polite"
      aria-label={message}
      className={cn(
        shellClass,
        "flex flex-col items-center justify-center gap-4 px-6 py-10 text-center",
        height && heightClass[height],
        className,
      )}
    >
      {children ?? <AnimeLoader size={loaderSize} label={message} />}
      <p className="text-sm text-ink-secondary">{message}</p>
    </div>
  );
}

export function LoadingPanel({
  message = "Loading...",
  height = "md",
  variant = "default",
  className,
  loaderSize,
}: {
  message?: string;
  height?: LoadingHeight;
  variant?: "default" | "ai";
  className?: string;
  loaderSize?: "sm" | "md" | "lg";
}) {
  return (
    <LoadingShell
      message={message}
      height={height}
      variant={variant}
      className={className}
      loaderSize={loaderSize ?? (height === "lg" ? "lg" : height === "sm" ? "sm" : "md")}
    />
  );
}

export function LoadingInline({
  message = "Loading...",
  className,
  loaderSize = "sm",
}: {
  message?: string;
  className?: string;
  loaderSize?: "sm" | "md" | "lg";
}) {
  return (
    <div
      role="status"
      aria-live="polite"
      aria-label={message}
      className={cn(
        cardClass,
        "flex items-center justify-center gap-3 border-line px-4",
        className,
      )}
    >
      <AnimeLoader size={loaderSize} label={message} />
      <p className="text-sm text-ink-secondary">{message}</p>
    </div>
  );
}

export function AiLoadingContent({
  message = "AI is thinking...",
  className,
}: {
  message?: string;
  className?: string;
}) {
  return (
    <div
      role="status"
      aria-live="polite"
      aria-label={message}
      className={cn("mt-3 flex items-center gap-3", className)}
    >
      <AnimeLoader size="sm" label={message} />
      <p className="text-sm text-ink-secondary">{message}</p>
    </div>
  );
}

export function AiLoadingPanel({
  message = "AI is thinking...",
  className,
}: {
  message?: string;
  className?: string;
}) {
  return <LoadingPanel message={message} height="sm" variant="ai" loaderSize="md" className={className} />;
}
