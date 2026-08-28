import { PageHeader } from "@/components/layout/page-header";
import { ResumePanel } from "@/features/candidate/components/resume-panel";

export default function CandidateResumePage() {
  return (
    <>
      <PageHeader
        crumbs={[{ label: "Candidate", href: "/candidate/dashboard" }, { label: "Resume" }]}
        title="Resume"
        description="Upload a PDF, DOC, or DOCX file. Extracted skills are decision-support information, not a hiring decision."
      />
      <ResumePanel />
    </>
  );
}
