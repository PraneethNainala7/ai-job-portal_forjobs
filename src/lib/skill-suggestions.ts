/** Common skills aligned with the backend matcher vocabulary. */
export const SKILL_SUGGESTIONS = [
  "Java",
  "Spring Boot",
  "Spring Framework",
  "Hibernate",
  "JPA",
  "Spring Security",
  "REST API",
  "Microservices",
  "Maven",
  "Gradle",
  "Kotlin",
  "C#",
  ".NET",
  "Python",
  "Django",
  "Flask",
  "FastAPI",
  "JavaScript",
  "TypeScript",
  "Node.js",
  "Express",
  "React",
  "Next.js",
  "Vue.js",
  "Angular",
  "HTML",
  "CSS",
  "Tailwind CSS",
  "SQL",
  "PostgreSQL",
  "MySQL",
  "MongoDB",
  "Redis",
  "Docker",
  "Kubernetes",
  "AWS",
  "Azure",
  "GCP",
  "Git",
  "CI/CD",
  "Jenkins",
  "GitHub Actions",
  "Agile",
  "Scrum",
  "OAuth",
  "JWT",
  "GraphQL",
  "Kafka",
  "RabbitMQ",
  "Go",
  "Rust",
  "PHP",
  "Ruby",
  "Swift",
  "Android",
  "iOS",
  "Figma",
  "System Design",
] as const;

function isSelected(skill: string, selected: readonly string[]) {
  const key = skill.toLowerCase();
  return selected.some((item) => item.toLowerCase() === key);
}

/** Strip bullets and normalize common pasted skill labels. */
export function normalizePastedSkill(raw: string): string {
  let skill = raw.trim().replace(/^[-•*]\s*/, "");
  if (!skill) return "";

  if (/^rest\s*apis?$/i.test(skill)) return "REST API";

  const versioned = skill.match(
    /^(Java|Python|Node\.js|TypeScript|Go|Rust|PHP|Ruby|Swift|Kotlin|C#|\.NET)\s+v?\d+(?:\.\d+)*$/i,
  );
  if (versioned) return versioned[1] === ".NET" ? ".NET" : titleCaseLanguage(versioned[1]);

  return skill;
}

function titleCaseLanguage(value: string) {
  const lower = value.toLowerCase();
  if (lower === "c#") return "C#";
  if (lower === "node.js") return "Node.js";
  return lower.charAt(0).toUpperCase() + lower.slice(1);
}

/** Split pasted text into individual skills (newlines, commas, semicolons, slashes). */
export function parsePastedSkills(text: string): string[] {
  const parts = text
    .split(/[\n\r]+|[,;]+|\s+\/\s+/)
    .map(normalizePastedSkill)
    .filter(Boolean);

  const seen = new Set<string>();
  const result: string[] = [];
  for (const part of parts) {
    const key = part.toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);
    result.push(part);
  }
  return result;
}

export function isMultiSkillPaste(text: string) {
  return parsePastedSkills(text).length > 1;
}

export function filterSkillSuggestions(
  query: string,
  selected: readonly string[],
  pool: readonly string[] = SKILL_SUGGESTIONS,
  limit = 8,
) {
  const needle = query.trim().toLowerCase();
  return pool
    .filter((skill) => !isSelected(skill, selected))
    .filter((skill) => !needle || skill.toLowerCase().includes(needle))
    .slice(0, limit);
}
