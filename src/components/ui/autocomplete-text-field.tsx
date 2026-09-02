"use client";

import {
  forwardRef,
  useEffect,
  useId,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
  type ComponentProps,
  type MutableRefObject,
  type Ref,
} from "react";
import { controlClass, labelClass } from "@/components/ui/control-styles";
import { filterCitySuggestions } from "@/lib/city-suggestions";
import { cn } from "@/lib/utils";

interface AutocompleteTextFieldProps extends Omit<ComponentProps<"input">, "id"> {
  label: string;
  hint?: string;
  error?: string;
  hideLabel?: boolean;
  dense?: boolean;
  wrapperClassName?: string;
  suggestions?: readonly string[];
  suggestionLimit?: number;
}

function useFieldParts({
  label,
  hint,
  error,
  hideLabel,
  dense,
  wrapperClassName,
  base,
}: {
  label: string;
  hint?: string;
  error?: string;
  hideLabel?: boolean;
  dense?: boolean;
  wrapperClassName?: string;
  base: string;
}) {
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

function mergeRefs<T>(...refs: Array<Ref<T> | undefined>) {
  return (node: T | null) => {
    for (const ref of refs) {
      if (!ref) continue;
      if (typeof ref === "function") ref(node);
      else (ref as MutableRefObject<T | null>).current = node;
    }
  };
}

export const AutocompleteTextField = forwardRef<HTMLInputElement, AutocompleteTextFieldProps>(
  function AutocompleteTextField(
    {
      label,
      hint,
      error,
      hideLabel,
      dense,
      wrapperClassName,
      suggestions,
      suggestionLimit = 8,
      className,
      defaultValue,
      value,
      onChange,
      onFocus,
      onBlur,
      ...rest
    },
    ref,
  ) {
    const base = useId();
    const listboxId = `${base}-suggestions`;
    const inputRef = useRef<HTMLInputElement>(null);
    const rootRef = useRef<HTMLDivElement>(null);
    const parts = useFieldParts({ label, hint, error, hideLabel, dense, wrapperClassName, base });

    const isControlled = value !== undefined;
    const [internalValue, setInternalValue] = useState(() => String(defaultValue ?? ""));
    const displayValue = isControlled ? String(value ?? "") : internalValue;
    const [open, setOpen] = useState(false);
    const [activeIndex, setActiveIndex] = useState(-1);

    const filteredSuggestions = useMemo(() => {
      if (!suggestions?.length) return [];
      return filterCitySuggestions(displayValue, suggestions, suggestionLimit);
    }, [displayValue, suggestions, suggestionLimit]);

    useEffect(() => {
      if (!open) return;
      function onPointerDown(event: MouseEvent) {
        if (!rootRef.current?.contains(event.target as Node)) {
          setOpen(false);
          setActiveIndex(-1);
        }
      }
      document.addEventListener("pointerdown", onPointerDown);
      return () => document.removeEventListener("pointerdown", onPointerDown);
    }, [open]);

    useEffect(() => {
      if (activeIndex >= filteredSuggestions.length) {
        setActiveIndex(filteredSuggestions.length > 0 ? 0 : -1);
      }
    }, [activeIndex, filteredSuggestions.length]);

    function emitChange(next: string) {
      const input = inputRef.current;
      if (!input) return;
      const descriptor = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value");
      descriptor?.set?.call(input, next);
      input.dispatchEvent(new Event("input", { bubbles: true }));
      input.dispatchEvent(new Event("change", { bubbles: true }));
    }

    function updateValue(next: string, event?: ChangeEvent<HTMLInputElement>) {
      if (!isControlled) {
        setInternalValue(next);
      }
      if (event) {
        onChange?.(event);
        return;
      }
      emitChange(next);
      onChange?.({
        target: { value: next, name: rest.name ?? "" },
        currentTarget: inputRef.current!,
      } as ChangeEvent<HTMLInputElement>);
    }

    function selectSuggestion(city: string) {
      updateValue(city);
      setOpen(false);
      setActiveIndex(-1);
      inputRef.current?.focus();
    }

    const showSuggestions = Boolean(suggestions?.length) && open && filteredSuggestions.length > 0;

    return (
      <div className={parts.wrapper}>
        {parts.labelNode}
        <div ref={rootRef} className="relative">
          <input
            {...rest}
            ref={mergeRefs(ref, inputRef)}
            id={parts.controlId}
            value={displayValue}
            role="combobox"
            aria-autocomplete="list"
            aria-expanded={showSuggestions}
            aria-controls={showSuggestions ? listboxId : undefined}
            aria-activedescendant={
              showSuggestions && activeIndex >= 0 ? `${base}-suggestion-${activeIndex}` : undefined
            }
            aria-describedby={parts.describedBy}
            aria-invalid={parts.invalid}
            autoComplete="off"
            onChange={(event) => {
              updateValue(event.target.value, event);
              setOpen(true);
              setActiveIndex(0);
            }}
            onFocus={(event) => {
              setOpen(true);
              onFocus?.(event);
            }}
            onBlur={(event) => {
              onBlur?.(event);
            }}
            onKeyDown={(event) => {
              if (event.key === "ArrowDown") {
                if (!filteredSuggestions.length) return;
                event.preventDefault();
                setOpen(true);
                setActiveIndex((current) => (current + 1) % filteredSuggestions.length);
                return;
              }
              if (event.key === "ArrowUp") {
                if (!filteredSuggestions.length) return;
                event.preventDefault();
                setOpen(true);
                setActiveIndex((current) =>
                  current <= 0 ? filteredSuggestions.length - 1 : current - 1,
                );
                return;
              }
              if (event.key === "Escape") {
                event.preventDefault();
                setOpen(false);
                setActiveIndex(-1);
                return;
              }
              if (event.key === "Enter" && activeIndex >= 0 && filteredSuggestions[activeIndex]) {
                event.preventDefault();
                selectSuggestion(filteredSuggestions[activeIndex]);
                return;
              }
              if (event.key === "Tab") {
                setOpen(false);
                setActiveIndex(-1);
              }
            }}
            className={cn(controlClass, dense && "h-11 lg:h-10", error && "border-danger", className)}
          />
          {showSuggestions ? (
            <ul
              id={listboxId}
              role="listbox"
              aria-label={`${label} suggestions`}
              className="absolute left-0 top-[calc(100%+0.375rem)] z-50 max-h-56 w-full overflow-auto rounded-[var(--radius-control)] border bg-surface p-1 shadow-lg"
            >
              {filteredSuggestions.map((city, index) => (
                <li
                  key={city}
                  id={`${base}-suggestion-${index}`}
                  role="option"
                  aria-selected={index === activeIndex}
                  onMouseEnter={() => setActiveIndex(index)}
                  onMouseDown={(event) => event.preventDefault()}
                  onClick={() => selectSuggestion(city)}
                  className={cn(
                    "cursor-pointer rounded-[calc(var(--radius-control)-2px)] px-3 py-2 text-sm",
                    index === activeIndex ? "bg-muted text-ink" : "text-ink-secondary hover:bg-muted/70",
                  )}
                >
                  {city}
                </li>
              ))}
            </ul>
          ) : null}
        </div>
        {parts.messages}
      </div>
    );
  },
);
