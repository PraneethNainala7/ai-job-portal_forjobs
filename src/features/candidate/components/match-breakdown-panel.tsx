import type { DimensionScore, MatchResult, ScoreBreakdownDetail, ScoreBreakdownV2, SkillMatchDetail } from "@/types/domain";
import { ReadMoreList, ReadMoreText } from "@/components/ui/read-more";

function SkillList({
  title,
  items,
  emptyLabel,
  collapsible = false,
}: {
  title: string;
  items?: string[];
  emptyLabel: string;
  collapsible?: boolean;
}) {
  const values = items ?? [];
  return (
    <>
      <h3 className="mt-4 text-sm font-semibold">{title}</h3>
      {collapsible ? (
        <ReadMoreList items={values} emptyLabel={emptyLabel} />
      ) : (
        <ul className="mt-1 space-y-1 text-sm text-ink-secondary">
          {values.length ? values.map((item) => <li key={item}>{item}</li>) : <li>{emptyLabel}</li>}
        </ul>
      )}
    </>
  );
}

function BreakdownRow({
  label,
  dimension,
}: {
  label: string;
  dimension?: DimensionScore;
}) {
  if (!dimension) {
    return null;
  }
  return (
    <div className="flex items-center justify-between gap-3 text-sm">
      <span className="text-ink-secondary">{label}</span>
      {dimension.status === "NOT_APPLICABLE" ? (
        <span className="text-xs text-ink-muted">Not specified by employer</span>
      ) : (
        <span className="font-mono tabular-nums text-ink">
          {dimension.earnedPoints ?? 0} / {dimension.maxPoints}
        </span>
      )}
    </div>
  );
}

function formatMatchType(type: SkillMatchDetail["matchType"]) {
  switch (type) {
    case "EXACT_MATCH":
      return "Exact";
    case "RELATED_MATCH":
      return "Related";
    case "TRANSFERABLE_MATCH":
      return "Transferable";
    default:
      return type;
  }
}

function SkillMatchList({ title, items }: { title: string; items?: SkillMatchDetail[] }) {
  const values = items ?? [];
  return (
    <>
      <h3 className="mt-4 text-sm font-semibold">{title}</h3>
      <ul className="mt-1 space-y-1 text-sm text-ink-secondary">
        {values.length ? (
          values.map((item) => (
            <li key={`${item.jobSkill}-${item.candidateSkill ?? "none"}`}>
              {item.jobSkill}
              {item.candidateSkill ? ` ← ${item.candidateSkill}` : ""}
              <span className="ml-2 text-xs uppercase tracking-wide text-ai">{formatMatchType(item.matchType)}</span>
            </li>
          ))
        ) : (
          <li>None identified</li>
        )}
      </ul>
    </>
  );
}

function mergeMissingRequiredSkills(match: MatchResult): string[] {
  const seen = new Set<string>();
  const merged: string[] = [];
  for (const skill of [
    ...(match.missingCriticalSkills ?? []),
    ...(match.missingRequiredSkills ?? []),
    ...(match.gaps ?? []),
  ]) {
    const key = skill.toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);
    merged.push(skill);
  }
  return merged;
}

function BreakdownDetailPanel({ breakdown }: { breakdown: ScoreBreakdownDetail }) {
  return (
    <div className="mt-4 space-y-2 rounded-[var(--radius-control)] border border-ai-border/60 bg-muted/20 p-3">
      <h3 className="text-sm font-semibold">Score breakdown</h3>
      <p className="text-xs text-ink-secondary">
        Related and transferable skill matches count toward applicable dimensions. Unspecified employer requirements are excluded from the total.
      </p>
      <BreakdownRow label="Required skills" dimension={breakdown.requiredSkills} />
      <BreakdownRow label="Preferred skills" dimension={breakdown.preferredSkills} />
      <BreakdownRow label="Relevant experience" dimension={breakdown.experience} />
      <BreakdownRow label="Role relevance" dimension={breakdown.roleRelevance} />
      <BreakdownRow label="Education / certifications" dimension={breakdown.education} />
      {breakdown.finalCalculation ? (
        <p className="pt-1 text-xs text-ink-muted">
          Normalized score: {breakdown.finalCalculation}
        </p>
      ) : null}
    </div>
  );
}

function LegacyBreakdownPanel({ breakdown }: { breakdown: ScoreBreakdownV2 }) {
  return (
    <div className="mt-4 space-y-2 rounded-[var(--radius-control)] border border-ai-border/60 bg-muted/20 p-3">
      <h3 className="text-sm font-semibold">Score breakdown</h3>
      <p className="text-xs text-ink-secondary">Related and transferable skill matches also count toward the totals below.</p>
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="text-ink-secondary">Required skills</span>
        <span className="font-mono tabular-nums text-ink">
          {breakdown.criticalRequiredSkills + breakdown.requiredSkills}
        </span>
      </div>
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="text-ink-secondary">Preferred skills</span>
        <span className="font-mono tabular-nums text-ink">{breakdown.preferredSkills}</span>
      </div>
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="text-ink-secondary">Relevant experience</span>
        <span className="font-mono tabular-nums text-ink">{breakdown.relevantExperience}</span>
      </div>
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="text-ink-secondary">Role relevance</span>
        <span className="font-mono tabular-nums text-ink">{breakdown.roleRelevance}</span>
      </div>
      <div className="flex items-center justify-between gap-3 text-sm">
        <span className="text-ink-secondary">Education / certifications</span>
        <span className="font-mono tabular-nums text-ink">{breakdown.educationAndCertifications}</span>
      </div>
    </div>
  );
}

export function MatchBreakdownPanel({ match }: { match: MatchResult }) {
  const breakdownDetail = match.scoreBreakdownDetail;
  const breakdownV2 = match.scoreBreakdownV2;
  const legacyBreakdown = match.scoreBreakdown;
  const missingRequiredSkills = mergeMissingRequiredSkills(match);

  return (
    <>
      {breakdownDetail ? (
        <BreakdownDetailPanel breakdown={breakdownDetail} />
      ) : breakdownV2 ? (
        <LegacyBreakdownPanel breakdown={breakdownV2} />
      ) : legacyBreakdown ? (
        <div className="mt-4 space-y-2 rounded-[var(--radius-control)] border border-ai-border/60 bg-muted/20 p-3">
          <h3 className="text-sm font-semibold">Score breakdown</h3>
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="text-ink-secondary">Required skills</span>
            <span className="font-mono tabular-nums text-ink">{legacyBreakdown.requiredSkills}</span>
          </div>
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="text-ink-secondary">Preferred skills</span>
            <span className="font-mono tabular-nums text-ink">{legacyBreakdown.preferredSkills}</span>
          </div>
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="text-ink-secondary">Experience</span>
            <span className="font-mono tabular-nums text-ink">{legacyBreakdown.experience}</span>
          </div>
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="text-ink-secondary">Role relevance</span>
            <span className="font-mono tabular-nums text-ink">{legacyBreakdown.roleRelevance}</span>
          </div>
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="text-ink-secondary">Education / certifications</span>
            <span className="font-mono tabular-nums text-ink">{legacyBreakdown.education}</span>
          </div>
        </div>
      ) : null}

      {match.scoreReliable === false && match.scoreUnreliableReason ? (
        <div className="mt-3 rounded-[var(--radius-control)] border border-warning/40 bg-warning/10 px-3 py-2">
          <p className="text-sm text-ink-secondary">{match.scoreUnreliableReason}</p>
        </div>
      ) : null}

      {match.scoreCapApplied ? (
        <div className="mt-3 rounded-[var(--radius-control)] border border-warning/40 bg-warning/10 px-3 py-2">
          <p className="text-sm font-medium text-ink">Score cap applied</p>
          {match.scoreCapReason ? (
            <ReadMoreText text={match.scoreCapReason} className="mt-1 text-sm leading-6 text-ink-secondary" limit={160} />
          ) : (
            <p className="mt-1 text-sm text-ink-secondary">Your score was limited by the matching policy.</p>
          )}
        </div>
      ) : null}

      <SkillMatchList title="Exact matches" items={match.matchedSkillDetails} />
      <SkillMatchList title="Partial matches" items={match.partiallyMatchedSkills} />
      <SkillList title="Matched skills" items={match.matchedSkills ?? match.strongAreas} emptyLabel="None identified yet" />
      <SkillList
        title="Partially relevant"
        items={match.partiallyRelevantSkills}
        emptyLabel="No transferable skills identified"
      />
      <SkillList
        title="Missing required skills"
        items={missingRequiredSkills}
        emptyLabel="No required-skill gaps"
        collapsible
      />
      <SkillList
        title="Missing preferred skills"
        items={match.missingPreferredSkills}
        emptyLabel="No preferred-skill gaps"
        collapsible
      />
      <SkillList title="Matched preferred skills" items={match.matchedPreferredSkills} emptyLabel="None matched" />
      {match.explanation ? (
        <div className="mt-4">
          <h3 className="text-sm font-semibold">Summary</h3>
          <ReadMoreText text={match.explanation} className="mt-1 text-sm leading-6 text-ink-secondary" />
        </div>
      ) : null}
    </>
  );
}
