"use client";

import { EyeIcon, EyeSlashIcon } from "@phosphor-icons/react";
import { forwardRef, useId, useState } from "react";
import { controlClass, labelClass } from "@/components/ui/control-styles";
import { cn } from "@/lib/utils";

interface PasswordFieldProps extends Omit<React.ComponentProps<"input">, "id" | "type"> {
  label: string;
  hint?: string;
  error?: string;
  hideLabel?: boolean;
  dense?: boolean;
  wrapperClassName?: string;
}

export const PasswordField = forwardRef<HTMLInputElement, PasswordFieldProps>(function PasswordField(
  { label, hint, error, hideLabel, dense, wrapperClassName, className, ...rest },
  ref,
) {
  const [visible, setVisible] = useState(false);
  const base = useId();
  const controlId = `${base}-control`;
  const hintId = hint ? `${base}-hint` : undefined;
  const errorId = error ? `${base}-error` : undefined;
  const describedBy = [hintId, errorId].filter(Boolean).join(" ") || undefined;

  return (
    <div className={cn(dense ? "space-y-1.5" : "space-y-2", wrapperClassName)}>
      <label htmlFor={controlId} className={cn(labelClass, hideLabel && "sr-only")}>
        {label}
      </label>
      <div className="relative">
        <input
          ref={ref}
          id={controlId}
          type={visible ? "text" : "password"}
          aria-describedby={describedBy}
          aria-invalid={Boolean(error) || undefined}
          className={cn(
            controlClass,
            dense && "h-11 lg:h-10",
            "pr-11",
            error && "border-danger",
            className,
          )}
          {...rest}
        />
        <button
          type="button"
          aria-label={visible ? "Hide password" : "Show password"}
          aria-pressed={visible}
          onClick={() => setVisible((current) => !current)}
          className="absolute inset-y-0 right-0 inline-flex w-11 items-center justify-center text-ink-secondary transition-colors hover:text-ink"
        >
          {visible ? <EyeSlashIcon size={18} aria-hidden /> : <EyeIcon size={18} aria-hidden />}
        </button>
      </div>
      {hint ? (
        <p id={hintId} className="text-xs leading-5 text-ink-secondary">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={errorId} role="alert" className="text-sm text-danger">
          {error}
        </p>
      ) : null}
    </div>
  );
});
