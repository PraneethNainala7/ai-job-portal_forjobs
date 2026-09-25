"use client";

import { useEffect, useId, useMemo, useRef, useState } from "react";
import { controlClass, labelClass } from "@/components/ui/control-styles";
import { filterSkillSuggestions, isMultiSkillPaste, normalizePastedSkill, parsePastedSkills } from "@/lib/skill-suggestions";
import { cn } from "@/lib/utils";

export function ChipInput({
  label,
  hint,
  values,
  onChange,
  addLabel = "Add",
  suggestions,
  suggestionLimit = 8,
}: {
  label: string;
  hint?: string;
  values: string[];
  onChange: (values: string[]) => void;
  addLabel?: string;
  suggestions?: readonly string[];
  suggestionLimit?: number;
}) {
  const base = useId();
  const inputId = `${base}-chip`;
  const listboxId = `${base}-suggestions`;
  const hintId = hint ? `${base}-hint` : undefined;
  const rootRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const [draft, setDraft] = useState("");
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);

  const filteredSuggestions = useMemo(() => {
    if (!suggestions?.length) return [];
    return filterSkillSuggestions(draft, values, suggestions, suggestionLimit);
  }, [draft, values, suggestions, suggestionLimit]);

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

  function addValues(incoming: string[]) {
    const seen = new Set(values.map((item) => item.toLowerCase()));
    const next = [...values];
    for (const value of incoming) {
      const trimmed = normalizePastedSkill(value) || value.trim();
      if (!trimmed) continue;
      const key = trimmed.toLowerCase();
      if (seen.has(key)) continue;
      seen.add(key);
      next.push(trimmed);
    }
    if (next.length !== values.length) {
      onChange(next);
    }
    setDraft("");
    setActiveIndex(-1);
    setOpen(false);
    inputRef.current?.focus();
  }

  function addValue(value: string) {
    addValues([value]);
  }

  function addDraft() {
    if (activeIndex >= 0 && filteredSuggestions[activeIndex]) {
      addValue(filteredSuggestions[activeIndex]);
      return;
    }
    addValue(draft);
  }

  const showSuggestions = Boolean(suggestions?.length) && open && filteredSuggestions.length > 0;

  return (
    <section>
      <label htmlFor={inputId} className={labelClass}>
        {label}
      </label>
      {hint ? (
        <p id={hintId} className="mt-1 text-xs leading-5 text-ink-secondary">
          {hint}
        </p>
      ) : null}
      {values.length ? (
        <ul className="mt-3 flex flex-wrap gap-2">
          {values.map((item) => (
            <li key={item} role="presentation" aria-hidden="true">
              <button
                type="button"
                aria-label={`Remove ${item}`}
                className="inline-flex items-center gap-1.5 rounded-full bg-primary-light px-3 py-1.5 text-sm text-primary"
                onClick={() => onChange(values.filter((value) => value !== item))}
              >
                {item}
                <span aria-hidden>×</span>
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="mt-3 text-sm text-ink-secondary">None added yet.</p>
      )}
      <div ref={rootRef} className="relative mt-3">
        <div className="flex flex-col gap-2 sm:flex-row">
          <input
            ref={inputRef}
            id={inputId}
            value={draft}
            role="combobox"
            aria-autocomplete="list"
            aria-expanded={showSuggestions}
            aria-controls={showSuggestions ? listboxId : undefined}
            aria-activedescendant={
              showSuggestions && activeIndex >= 0 ? `${base}-suggestion-${activeIndex}` : undefined
            }
            aria-describedby={hintId}
            placeholder={suggestions?.length ? "Type, paste, or pick skills" : undefined}
            onChange={(event) => {
              setDraft(event.target.value);
              setOpen(true);
              setActiveIndex(0);
            }}
            onPaste={(event) => {
              const pasted = event.clipboardData.getData("text");
              if (!isMultiSkillPaste(pasted)) return;
              event.preventDefault();
              addValues(parsePastedSkills(pasted));
            }}
            onFocus={() => setOpen(true)}
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
              if (event.key !== "Enter") return;
              event.preventDefault();
              addDraft();
            }}
            className={cn(controlClass, "sm:flex-1")}
          />
          <button
            type="button"
            className="h-11 rounded-[var(--radius-control)] border px-4 text-sm font-semibold"
            onClick={addDraft}
          >
            {addLabel}
          </button>
        </div>
        {showSuggestions ? (
          <ul
            id={listboxId}
            role="listbox"
            aria-label={`${label} suggestions`}
            className="absolute left-0 top-[calc(100%+0.375rem)] z-50 max-h-56 w-full overflow-auto rounded-[var(--radius-control)] border bg-surface p-1 shadow-lg"
          >
            {filteredSuggestions.map((skill, index) => (
              <li
                key={skill}
                id={`${base}-suggestion-${index}`}
                role="option"
                aria-selected={index === activeIndex}
                onMouseEnter={() => setActiveIndex(index)}
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => addValue(skill)}
                className={cn(
                  "cursor-pointer rounded-[calc(var(--radius-control)-2px)] px-3 py-2 text-sm",
                  index === activeIndex ? "bg-muted text-ink" : "text-ink-secondary hover:bg-muted/70",
                )}
              >
                {skill}
              </li>
            ))}
          </ul>
        ) : null}
      </div>
    </section>
  );
}
