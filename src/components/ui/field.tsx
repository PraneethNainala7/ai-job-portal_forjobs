"use client";

import { useId } from "react";
import { controlClass, labelClass, textAreaClass } from "@/components/ui/control-styles";
import { cn } from "@/lib/utils";

interface FieldProps {
  label: string;
  hint?: string;
  error?: string;
  hideLabel?: boolean;
  /** Tightens spacing and trims control height on pointer-sized screens only. */
  dense?: boolean;
  wrapperClassName?: string;
}

const denseControl = "h-11 lg:h-10";

type TextFieldProps = FieldProps & Omit<React.ComponentProps<"input">, "id">;
type TextAreaFieldProps = FieldProps & Omit<React.ComponentProps<"textarea">, "id">;

function useFieldParts({ label, hint, error, hideLabel, dense, wrapperClassName }: FieldProps) {
  const base = useId();
  const controlId = `${base}-control`;
  const hintId = hint ? `${base}-hint` : undefined;
  const errorId = error ? `${base}-error` : undefined;

  return {
    controlId,
    describedBy: [hintId, errorId].filter(Boolean).join(" ") || undefined,
    invalid: Boolean(error) || undefined,
    wrapper: cn(dense ? "space-y-1.5" : "space-y-2", wrapperClassName),
    labelNode: (
      <label htmlFor={controlId} className={cn(labelClass, hideLabel && "sr-only")}>
        {label}
      </label>
    ),
    messages: (
      <>
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
      </>
    ),
  };
}

export function TextField({
  label,
  hint,
  error,
  hideLabel,
  dense,
  wrapperClassName,
  className,
  ...rest
}: TextFieldProps) {
  const parts = useFieldParts({ label, hint, error, hideLabel, dense, wrapperClassName });
  return (
    <div className={parts.wrapper}>
      {parts.labelNode}
      <input
        id={parts.controlId}
        aria-describedby={parts.describedBy}
        aria-invalid={parts.invalid}
        className={cn(controlClass, dense && denseControl, error && "border-danger", className)}
        {...rest}
      />
      {parts.messages}
    </div>
  );
}

export function TextAreaField({
  label,
  hint,
  error,
  hideLabel,
  dense,
  wrapperClassName,
  className,
  rows = 5,
  ...rest
}: TextAreaFieldProps) {
  const parts = useFieldParts({ label, hint, error, hideLabel, dense, wrapperClassName });
  return (
    <div className={parts.wrapper}>
      {parts.labelNode}
      <textarea
        id={parts.controlId}
        rows={rows}
        aria-describedby={parts.describedBy}
        aria-invalid={parts.invalid}
        className={cn(textAreaClass, error && "border-danger", className)}
        {...rest}
      />
      {parts.messages}
    </div>
  );
}
