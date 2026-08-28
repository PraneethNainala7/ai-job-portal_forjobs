"use client";

import { BriefcaseIcon, UserIcon } from "@phosphor-icons/react";
import { animate, createDrawable, createScope, stagger, utils } from "animejs";
import { useEffect, useRef } from "react";

const FIELD: Array<[number, number]> = [
  [10, 12],
  [22, 8],
  [36, 14],
  [51, 7],
  [66, 11],
  [80, 9],
  [90, 18],
  [8, 30],
  [92, 34],
  [10, 50],
  [90, 54],
  [9, 70],
  [20, 84],
  [40, 90],
  [62, 88],
  [80, 82],
  [90, 72],
  [16, 62],
  [84, 24],
  [28, 6],
];

const MATCHES = [
  { x: "72%", y: "26%", score: 87 },
  { x: "22%", y: "40%", score: 74 },
  { x: "68%", y: "72%", score: 81 },
] as const;

const IDLE = [
  { x: "16%", y: "18%", size: "size-7" },
  { x: "84%", y: "48%", size: "size-8" },
  { x: "38%", y: "82%", size: "size-7" },
  { x: "14%", y: "70%", size: "size-6" },
  { x: "84%", y: "16%", size: "size-6" },
] as const;

const LINKS = [
  "M 50 50 C 60 36, 68 28, 72 26",
  "M 50 50 C 36 46, 26 42, 22 40",
  "M 50 50 C 58 62, 64 70, 68 72",
];

export function AuthGraphic() {
  const root = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = root.current;
    if (!el) return;

    const scope = createScope({
      root: el,
      mediaQueries: {
        desktop: "(min-width: 1024px)",
        reduce: "(prefers-reduced-motion: reduce)",
      },
    }).add((self) => {
      if (!self || !self.matches.desktop || self.matches.reduce) return;

      animate(".auth-orbit-a", {
        rotate: 360,
        duration: 42000,
        ease: "linear",
        loop: true,
      });

      animate(".auth-orbit-b", {
        rotate: -360,
        duration: 56000,
        ease: "linear",
        loop: true,
      });

      animate(".auth-field", {
        opacity: stagger([0.18, 0.55], { from: "center" }),
        duration: 2400,
        ease: "inOutSine",
        loop: true,
        alternate: true,
      });

      animate(".auth-role", {
        scale: [1, 1.045, 1],
        duration: 3200,
        ease: "inOut(2)",
        loop: true,
      });

      animate(".auth-idle", {
        x: () => utils.random(-6, 6),
        y: () => utils.random(-6, 6),
        duration: () => utils.random(2800, 4800),
        loop: true,
        composition: "blend",
        ease: "inOut(3)",
      });

      animate(createDrawable(el.querySelectorAll(".auth-link")), {
        draw: ["0 0", "0 1", "1 1"],
        delay: stagger(320),
        duration: 2800,
        ease: "inOut(3)",
        loop: true,
      });

      animate(".auth-match", {
        scale: [1, 1.06, 1],
        duration: 2400,
        delay: stagger(320),
        ease: "inOutSine",
        loop: true,
      });
    });

    return () => scope.revert();
  }, []);

  return (
    <div className="flex h-full min-h-0 min-w-0 items-center justify-center overflow-hidden [container-type:size]">
      <div
        ref={root}
        aria-hidden
        className="relative overflow-hidden"
        style={{
          width: "min(100cqw, 100cqh, 22rem)",
          height: "min(100cqw, 100cqh, 22rem)",
        }}
      >
        <div className="absolute inset-0 rounded-full bg-[radial-gradient(circle,rgb(79_70_229_/_0.22),transparent_64%)]" />

        {FIELD.map(([x, y]) => (
          <span
            key={`${x}-${y}`}
            className="auth-field absolute size-1 rounded-full bg-white/70"
            style={{ left: `${x}%`, top: `${y}%` }}
          />
        ))}

        <svg
          className="absolute inset-0 h-full w-full"
          viewBox="0 0 100 100"
          fill="none"
          overflow="hidden"
        >
          <g className="auth-orbit-a origin-center" style={{ transformBox: "fill-box", transformOrigin: "center" }}>
            <ellipse cx="50" cy="50" rx="31" ry="29" className="stroke-white/15" strokeWidth="0.35" />
          </g>
          <g className="auth-orbit-b origin-center" style={{ transformBox: "fill-box", transformOrigin: "center" }}>
            <ellipse cx="50" cy="50" rx="40" ry="34" className="stroke-primary/35" strokeWidth="0.4" strokeDasharray="2.2 3.4" />
          </g>
          {LINKS.map((d) => (
            <path
              key={d}
              className="auth-link stroke-primary/80"
              d={d}
              strokeWidth="0.7"
              strokeLinecap="round"
            />
          ))}
        </svg>

        {IDLE.map((node) => (
          <div
            key={`${node.x}-${node.y}`}
            className="absolute"
            style={{ left: node.x, top: node.y, transform: "translate(-50%, -50%)" }}
          >
            <span className={`auth-idle grid ${node.size} place-items-center rounded-full border border-white/15 bg-white/5 text-white/35`}>
              <UserIcon size={12} weight="bold" />
            </span>
          </div>
        ))}

        {MATCHES.map((node) => (
          <div
            key={node.score}
            className="absolute"
            style={{ left: node.x, top: node.y, transform: "translate(-50%, -50%)" }}
          >
            <span className="auth-match grid size-11 place-items-center rounded-full bg-white text-brand shadow-[0_0_0_3px_rgb(79_70_229_/_0.45)]">
              <UserIcon size={18} weight="bold" />
            </span>
            <span className="auth-score absolute left-1/2 top-[calc(100%+0.3rem)] -translate-x-1/2 font-mono text-xs tabular-nums text-white/85">
              {node.score}
            </span>
          </div>
        ))}

        <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
          <span className="auth-role grid size-[4.5rem] place-items-center rounded-[1.15rem] bg-primary text-white shadow-[0_18px_40px_rgb(79_70_229_/_0.35)]">
            <BriefcaseIcon size={28} weight="fill" />
          </span>
        </div>
      </div>
    </div>
  );
}
