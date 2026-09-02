export type RegisterRole = "CANDIDATE" | "EMPLOYER";

export type RegisterCopy = {
  title: string;
  description: string;
  asideHeadline: string;
  asideBody: string;
  asideFootnote: string;
};

const candidateCopy: RegisterCopy = {
  title: "Join as a candidate",
  description: "Upload a resume, browse roles with match scores on every card, and apply when you are ready.",
  asideHeadline: "Find roles with explainable fit.",
  asideBody:
    "AI reads your resume and surfaces match scores and skill gaps. Find jobs lists every open role; recommendations highlight strong matches at 60% or higher.",
  asideFootnote: "Match insights support your search. They do not apply for you.",
};

const employerCopy: RegisterCopy = {
  title: "Register your company",
  description: "Add company details and CIN. Job posting unlocks after admin approval.",
  asideHeadline: "Hiring decisions stay human.",
  asideBody: "The AI reads resumes and surfaces fit for your applicants. Shortlist and reject stay with you.",
  asideFootnote: "Match insights support decisions. They do not hire for you.",
};

export function getRegisterCopy(role: RegisterRole): RegisterCopy {
  return role === "EMPLOYER" ? employerCopy : candidateCopy;
}

export function getEmployerCompanyStepCopy(): RegisterCopy {
  return {
    title: "Complete your company profile",
    description: "Add your company details so we can review your employer account.",
    asideHeadline: "Almost there.",
    asideBody: "Once submitted, our team reviews your company profile. You can track approval status from your workspace.",
    asideFootnote: "Job posting unlocks after admin approval.",
  };
}

export function parseRegisterRole(value: string | string[] | undefined): RegisterRole {
  const raw = Array.isArray(value) ? value[0] : value;
  return raw === "EMPLOYER" ? "EMPLOYER" : "CANDIDATE";
}
