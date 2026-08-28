import type { Icon } from "@phosphor-icons/react";
import type { ButtonHTMLAttributes } from "react";
import { cn } from "@/lib/utils";

const variants = {
  primary:
    "border border-transparent bg-primary text-white shadow-[0_1px_0_rgb(67_56_202_/_0.35)] hover:bg-primary-hover",
  secondary:
    "border border-input bg-surface text-ink hover:border-ink-muted hover:bg-muted",
  ghost: "border border-transparent text-ink-secondary hover:bg-muted hover:text-ink",
  danger: "border border-transparent bg-danger text-white hover:opacity-90",
  link: "h-auto border-transparent px-0 text-primary hover:text-primary-hover",
} as const;

const sizes = {
  sm: "h-10 px-3 text-sm",
  md: "h-11 px-4 text-sm",
} as const;

export type ButtonVariant = keyof typeof variants;
export type ButtonSize = keyof typeof sizes;

export function buttonClass({
  variant = "primary",
  size = "md",
  className,
}: {
  variant?: ButtonVariant;
  size?: ButtonSize;
  className?: string;
} = {}) {
  return cn(
    "inline-flex items-center justify-center gap-2 rounded-[var(--radius-control)] font-semibold transition-[color,background-color,border-color,transform] duration-150 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-70",
    variants[variant],
    variant !== "link" && sizes[size],
    className,
  );
}

export function Button({
  variant = "primary",
  size = "md",
  icon: Icon,
  className,
  children,
  type = "button",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
  icon?: Icon;
}) {
  return (
    <button type={type} className={buttonClass({ variant, size, className })} {...props}>
      {Icon ? <Icon size={16} weight="bold" aria-hidden /> : null}
      {children}
    </button>
  );
}
