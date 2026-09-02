export type UserRole = "ADMIN" | "CANDIDATE" | "EMPLOYER";

export type AccountStatus =
  | "PENDING"
  | "ACTIVE"
  | "ON_HOLD"
  | "REJECTED"
  | "INACTIVE";

export type ApplicationStatus =
  | "APPLIED"
  | "UNDER_REVIEW"
  | "SHORTLISTED"
  | "REJECTED"
  | "INTERVIEW"
  | "SELECTED";

export interface SessionUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  accountStatus: AccountStatus;
  companyName?: string;
  companyInformation?: string;
  companyLocation?: string;
  cin?: string;
  companyWebsite?: string;
  rejectionReason?: string;
}

export interface CandidateProfile {
  id: string;
  fullName: string;
  email: string;
  phone?: string;
  location?: string;
  title?: string;
  experience?: string;
  skills: string[];
  education?: string[];
  certifications?: string[];
  portfolioUrl?: string;
  linkedinUrl?: string;
}

export interface Job {
  id: string;
  role: string;
  companyName: string;
  experience: string;
  skills: string[];
  criticalSkills?: string[];
  preferredSkills?: string[];
  educationRequirements?: string[];
  certificationRequirements?: string[];
  location: string;
  salary: string;
  jobType: string;
  workMode?: string;
  description: string;
  status: string;
  postedDate?: string;
  employerId?: string;
}

export interface JobListResponse {
  items: Array<Job & { matchScore?: number }>;
  total: number;
  page: number;
  pageSize: number;
}

export interface MatchScoreBreakdown {
  requiredSkills: number;
  preferredSkills: number;
  experience: number;
  roleRelevance: number;
  semanticSimilarity?: number;
  education: number;
  criticalRequiredSkills?: number;
  relevantExperience?: number;
  educationAndCertifications?: number;
}

export interface SkillMatchDetail {
  jobSkill: string;
  candidateSkill?: string | null;
  matchType: "EXACT_MATCH" | "RELATED_MATCH" | "TRANSFERABLE_MATCH" | "NO_MATCH";
}

export type DimensionStatus = "APPLICABLE" | "NOT_APPLICABLE";

export interface DimensionScore {
  earnedPoints: number | null;
  maxPoints: number;
  status: DimensionStatus;
}

export interface ScoreBreakdownDetail {
  requiredSkills: DimensionScore;
  preferredSkills: DimensionScore;
  experience: DimensionScore;
  roleRelevance: DimensionScore;
  education: DimensionScore;
  totalEarnedPoints: number;
  applicableMaximumPoints: number;
  finalCalculation?: string | null;
}

export interface ScoreBreakdownV2 {
  criticalRequiredSkills: number;
  requiredSkills: number;
  preferredSkills: number;
  relevantExperience: number;
  roleRelevance: number;
  educationAndCertifications: number;
}

export interface MatchResult {
  jobId: string;
  candidateId: string;
  matchScore: number;
  strongAreas: string[];
  gaps: string[];
  explanation?: string;
  matchedSkills?: string[];
  partiallyRelevantSkills?: string[];
  missingRequiredSkills?: string[];
  missingPreferredSkills?: string[];
  scoreBreakdown?: MatchScoreBreakdown;
  matchedSkillDetails?: SkillMatchDetail[];
  partiallyMatchedSkills?: SkillMatchDetail[];
  missingCriticalSkills?: string[];
  matchedPreferredSkills?: string[];
  scoreCapApplied?: boolean;
  scoreCapReason?: string | null;
  scoreBreakdownV2?: ScoreBreakdownV2;
  scoreBreakdownDetail?: ScoreBreakdownDetail;
  totalEarnedPoints?: number;
  applicableMaximumPoints?: number;
  scoreReliable?: boolean;
  scoreUnreliableReason?: string | null;
}

export interface RecommendationResponse {
  items: Array<{ job: Job & { matchScore?: number }; match: MatchResult }>;
  minScore: number;
  evaluatedCount: number;
  resumeReady: boolean;
}

export interface Application {
  id: string;
  jobId: string;
  candidateId: string;
  status: ApplicationStatus;
  appliedAt: string;
  rejectionReason?: string;
}

export interface EmployerJob extends Job {
  applicantCount: number;
  shortlistedCount: number;
}

export interface ApplicationResumeInfo {
  fileName: string;
  appliedAt: string;
  downloadable: boolean;
}

export interface EmployerApplicant {
  application: Application;
  candidate: CandidateProfile;
  match?: MatchResult | null;
  resume?: ApplicationResumeInfo;
}

export interface InterviewQuestions {
  technical: string[];
  resumeBased: string[];
  jobSpecific: string[];
  behavioral: string[];
}

export interface ResumeAnalysis {
  status: "PROCESSING" | "COMPLETE" | "FAILED";
  fileName: string;
  uploadedAt: string;
  skills: string[];
  additionalSkills: string[];
  experience?: string;
  titles: string[];
  education: string[];
  certifications: string[];
  seniority?: string;
  technologies?: string[];
  projects?: string[];
  industries?: string[];
  programmingLanguages?: string[];
  frameworks?: string[];
  databases?: string[];
  cloudTechnologies?: string[];
  tools?: string[];
  error?: string;
}

export interface ApplicationRecord extends Application {
  job: Job;
}

export interface AuthResponse {
  user: SessionUser;
}

export interface GoogleAuthResponse extends AuthResponse {
  needsEmployerOnboarding: boolean;
}

export interface AdminAccount extends SessionUser {
  createdAt: string;
  holdReason?: string;
}

export interface AuditLog {
  id: string;
  adminId: string;
  adminName: string;
  action: string;
  targetType: string;
  targetId: string;
  previousStatus?: string;
  newStatus?: string;
  reason?: string;
  createdAt: string;
}

export interface AdminJob extends Job {
  employerName?: string;
  applicantCount: number;
}

export interface AdminApplication {
  id: string;
  jobRole: string;
  companyName: string;
  employerName?: string;
  candidateName: string;
  status: ApplicationStatus;
  appliedAt: string;
}

export interface AdminStats {
  candidates: number;
  employers: number;
  users: number;
  jobs: number;
  activeJobs: number;
  closedJobs: number;
  applications: number;
  pendingEmployers: number;
}
