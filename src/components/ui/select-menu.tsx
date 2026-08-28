"use client";

import { CaretDownIcon, CheckIcon, type Icon } from "@phosphor-icons/react";
import { useEffect, useId, useRef, useState } from "react";
import { labelClass } from "@/components/ui/control-styles";
import { cn } from "@/lib/utils";

export interface SelectMenuOption {
  value: string;
  label: string;
  icon?: Icon;
}

export function SelectMenu({
  label,
  options,
  value,
  defaultValue,
  onValueChange,
  name,
  menuLabel,
  placeholder = "Select an option",
  hint,
  error,
  dense,
  hideLabel,
  wrapperClassName,
}: {
  label: string;
  options: SelectMenuOption[];
  value?: string;
  defaultValue?: string;
  onValueChange?: (value: string) => void;
  name?: string;
  menuLabel?: string;
  placeholder?: string;
  hint?: string;
  error?: string;
  dense?: boolean;
  hideLabel?: boolean;
  wrapperClassName?: string;
}) {
  const base = useId();
  const labelId = `${base}-label`;
  const valueId = `${base}-value`;
  const listboxId = `${base}-listbox`;
  const hintId = hint ? `${base}-hint` : undefined;
  const errorId = error ? `${base}-error` : undefined;

  const [internal, setInternal] = useState(defaultValue ?? "");
  const selected = value ?? internal;
  const selectedIndex = options.findIndex((option) => option.value === selected);
  const selectedOption = selectedIndex >= 0 ? options[selectedIndex] : undefined;

  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);
  const rootRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    listRef.current?.focus();

    function onPointerDown(event: PointerEvent) {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    }
    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, [open]);

  function openMenu(index: number) {
    setActiveIndex(index < 0 ? 0 : index);
    setOpen(true);
  }

  function close(focusTrigger = true) {
    setOpen(false);
    if (focusTrigger) triggerRef.current?.focus();
  }

  function commit(index: number) {
    const option = options[index];
    if (!option) return;
    if (value === undefined) setInternal(option.value);
    onValueChange?.(option.value);
    close();
  }

  function onTriggerKeyDown(event: React.KeyboardEvent<HTMLButtonElement>) {
    if (event.key === "ArrowDown" || event.key === "ArrowUp" || event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openMenu(selectedIndex);
    }
  }

  function onListKeyDown(event: React.KeyboardEvent<HTMLDivElement>) {
    switch (event.key) {
      case "ArrowDown":
        event.preventDefault();
        setActiveIndex((current) => (current + 1) % options.length);
        break;
      case "ArrowUp":
        event.preventDefault();
        setActiveIndex((current) => (current - 1 + options.length) % options.length);
        break;
      case "Home":
        event.preventDefault();
        setActiveIndex(0);
        break;
      case "End":
        event.preventDefault();
        setActiveIndex(options.length - 1);
        break;
      case "Enter":
      case " ":
        event.preventDefault();
        commit(activeIndex);
        break;
      case "Escape":
        event.preventDefault();
        close();
        break;
      case "Tab":
        close(false);
        break;
      default:
        break;
    }
  }

  return (
    <div ref={rootRef} className={cn(dense ? "space-y-1.5" : "space-y-2", wrapperClassName)}>
      <span id={labelId} className={cn(labelClass, hideLabel && "sr-only")}>
        {label}
      </span>
      <div className="relative">
        <button
          ref={triggerRef}
          type="button"
          role="combobox"
          aria-haspopup="listbox"
          aria-controls={listboxId}
          aria-expanded={open}
          aria-labelledby={`${labelId} ${valueId}`}
          aria-describedby={[hintId, errorId].filter(Boolean).join(" ") || undefined}
          aria-invalid={Boolean(error) || undefined}
          onClick={() => (open ? close() : openMenu(selectedIndex))}
          onKeyDown={onTriggerKeyDown}
          className={cn(
            "flex h-11 w-full items-center justify-between gap-2 rounded-[var(--radius-control)] border border-input bg-surface px-3 text-left text-sm text-ink",
            dense && "lg:h-10",
            error && "border-danger",
          )}
        >
          <span className="flex min-w-0 items-center gap-2">
            {selectedOption?.icon ? (
              <selectedOption.icon size={16} aria-hidden className="shrink-0 text-ink-secondary" />
            ) : null}
            <span id={valueId} className={cn("truncate", !selectedOption && "text-ink-muted")}>
              {selectedOption?.label ?? placeholder}
            </span>
          </span>
          <CaretDownIcon
            size={16}
            aria-hidden
            className={cn("shrink-0 text-ink-secondary transition-transform", open && "rotate-180")}
          />
        </button>

        {open ? (
          <div
            ref={listRef}
            id={listboxId}
            role="listbox"
            tabIndex={-1}
            aria-labelledby={labelId}
            aria-activedescendant={`${base}-option-${activeIndex}`}
            onKeyDown={onListKeyDown}
            className="absolute left-0 top-[calc(100%+0.375rem)] z-50 max-h-72 w-full min-w-56 overflow-auto rounded-[var(--radius-control)] border bg-surface p-1 shadow-lg outline-none"
          >
            {menuLabel ? (
              <p className="px-2 py-1.5 text-xs font-medium text-ink-secondary">{menuLabel}</p>
            ) : null}
            {options.map((option, index) => {
              const isSelected = option.value === selected;
              const OptionIcon = option.icon;
              return (
                <div
                  key={option.value}
                  id={`${base}-option-${index}`}
                  role="option"
                  aria-selected={isSelected}
                  onClick={() => commit(index)}
                  onPointerMove={() => setActiveIndex(index)}
                  className={cn(
                    "flex cursor-pointer items-center gap-2 rounded-[calc(var(--radius-control)-2px)] px-2 py-2 text-sm",
                    index === activeIndex ? "bg-muted text-ink" : "text-ink",
                  )}
                >
                  <span className="grid size-4 shrink-0 place-items-center text-primary">
                    {isSelected ? <CheckIcon size={14} weight="bold" aria-hidden /> : null}
                  </span>
                  {OptionIcon ? (
                    <OptionIcon size={16} aria-hidden className="shrink-0 text-ink-secondary" />
                  ) : null}
                  <span className="truncate">{option.label}</span>
                </div>
              );
            })}
          </div>
        ) : null}
      </div>
      {name ? <input type="hidden" name={name} value={selected} /> : null}
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
}
