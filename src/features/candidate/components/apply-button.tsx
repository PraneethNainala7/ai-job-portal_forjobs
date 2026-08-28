"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { AnimeLoader } from "@/components/ui/anime-loader";
import { Button, buttonClass } from "@/components/ui/button";
import { applyRequest, getApplicationsRequest } from "@/lib/api/candidate";
import { hasAppliedToJob } from "@/lib/application-status";

export function ApplyButton({ jobId }: { jobId: string }) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [error, setError] = useState("");
  const { data: applications, isLoading } = useQuery({
    queryKey: ["applications"],
    queryFn: getApplicationsRequest,
  });
  const alreadyApplied = hasAppliedToJob(applications?.items ?? [], jobId);

  const mutation = useMutation({
    mutationFn: () => applyRequest(jobId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["applications"] });
      router.push("/candidate/applications");
    },
    onError: (err: Error) => setError(err.message),
  });

  if (isLoading) {
    return (
      <div
        role="status"
        aria-live="polite"
        aria-label="Checking application status..."
        className="flex h-11 items-center justify-center"
      >
        <AnimeLoader size="sm" label="Checking application status..." />
      </div>
    );
  }

  return (
    <div>
      <Button
        type="button"
        onClick={() => {
          setError("");
          mutation.mutate();
        }}
        disabled={alreadyApplied || mutation.isPending}
        className="w-full"
      >
        {mutation.isPending ? (
          <>
            <AnimeLoader size="sm" label="Submitting application..." />
            Submitting...
          </>
        ) : alreadyApplied ? (
          "Already applied"
        ) : (
          "Apply for job"
        )}
      </Button>
      {alreadyApplied ? (
        <Link href="/candidate/applications" className={buttonClass({ variant: "link", className: "mt-3 block text-center" })}>
          View application
        </Link>
      ) : null}
      {error ? <p className="mt-3 text-sm text-danger">{error}</p> : null}
    </div>
  );
}
