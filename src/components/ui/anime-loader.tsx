import { cn } from "@/lib/utils";

const sizeClass = {
  sm: "anime-loader--sm",
  md: "anime-loader--md",
  lg: "anime-loader--lg",
} as const;

export type AnimeLoaderSize = keyof typeof sizeClass;

export function AnimeLoader({
  size = "md",
  label = "Loading",
  className,
}: {
  size?: AnimeLoaderSize;
  label?: string;
  className?: string;
}) {
  return (
    <div
      className={cn("anime-loader", sizeClass[size], className)}
      role="img"
      aria-label={label}
    >
      <span className="anime-loader__track" aria-hidden />
      <span className="anime-loader__arc" aria-hidden />
      <span className="anime-loader__inner" aria-hidden />
    </div>
  );
}
