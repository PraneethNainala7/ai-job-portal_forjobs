"use client";

import { useId, useState } from "react";
import { controlClass, labelClass } from "@/components/ui/control-styles";

export function ChipInput({
  label,
  hint,
  values,
  onChange,
  addLabel = "Add",
}: {
  label: string;
  hint?: string;
  values: string[];
  onChange: (values: string[]) => void;
  addLabel?: string;
}) {
  const base = useId();
  const inputId = `${base}-chip`;
  const hintId = hint ? `${base}-hint` : undefined;
  const [draft, setDraft] = useState("");

  function add() {
    const value = draft.trim();
    if (!value) return;
    if (!values.some((item) => item.toLowerCase() === value.toLowerCase())) {
      onChange([...values, value]);
    }
    setDraft("");
  }

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
            <li key={item}>
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
      <div className="mt-3 flex flex-col gap-2 sm:flex-row">
        <input
          id={inputId}
          value={draft}
          aria-describedby={hintId}
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key !== "Enter") return;
            event.preventDefault();
            add();
          }}
          className={`${controlClass} sm:flex-1`}
        />
        <button
          type="button"
          className="h-11 rounded-[var(--radius-control)] border px-4 text-sm font-semibold"
          onClick={add}
        >
          {addLabel}
        </button>
      </div>
    </section>
  );
}
