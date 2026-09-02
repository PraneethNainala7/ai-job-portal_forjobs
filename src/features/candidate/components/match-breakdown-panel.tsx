import type { MatchResult, ScoreBreakdownV2, SkillMatchDetail } from "@/types/domain";
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

function BreakdownRow({ label, value }: { label: string; value: number }) {
  return (
    <div className="flex items-center justify-between gap-3 text-sm">
      <span className="text-ink-secondary">{label}</span>
      <span className="font-mono tabular-nums text-ink">{value}</span>
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

function BreakdownPanel({ breakdown }: { breakdown: ScoreBreakdownV2 }) {
  return (
    <div className="mt-4 space-y-2 rounded-[var(--radius-control)] border border-ai-border/60 bg-muted/20 p-3">
      <h3 className="text-sm font-semibold">Score breakdown</h3>
      <BreakdownRow label="Critical skills" value={breakdown.criticalRequiredSkills} />
      <BreakdownRow label="Required skills" value={breakdown.requiredSkills} />
      <BreakdownRow label="Preferred skills" value={breakdown.preferredSkills} />
      <BreakdownRow label="Relevant experience" value={breakdown.relevantExperience} />
      <BreakdownRow label="Role relevance" value={breakdown.roleRelevance} />
      <BreakdownRow label="Education / certifications" value={breakdown.educationAndCertifications} />
    </div>
  );
}

export function MatchBreakdownPanel({ match }: { match: MatchResult }) {
  const breakdownV2 = match.scoreBreakdownV2;
  const legacyBreakdown = match.scoreBreakdown;

  return (
    <>
      {breakdownV2 ? (
        <BreakdownPanel breakdown={breakdownV2} />
      ) : legacyBreakdown ? (
        <div className="mt-4 space-y-2 rounded-[var(--radius-control)] border border-ai-border/60 bg-muted/20 p-3">
          <h3 className="text-sm font-semibold">Score breakdown</h3>
          <BreakdownRow label="Required skills" value={legacyBreakdown.requiredSkills} />
          <BreakdownRow label="Preferred skills" value={legacyBreakdown.preferredSkills} />
          <BreakdownRow label="Experience" value={legacyBreakdown.experience} />
          <BreakdownRow label="Role relevance" value={legacyBreakdown.roleRelevance} />
          <BreakdownRow label="Education / certifications" value={legacyBreakdown.education} />
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
        title="Missing critical skills"
        items={match.missingCriticalSkills}
        emptyLabel="No critical-skill gaps"
        collapsible
      />
      <SkillList
        title="Missing required skills"
        items={match.missingRequiredSkills ?? match.gaps}
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
