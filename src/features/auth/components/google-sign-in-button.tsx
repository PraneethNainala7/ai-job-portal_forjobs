"use client";

import type { UserRole } from "@/types/domain";
import { AuthRequestError, googleAuthRequest } from "@/lib/api/auth";
import { getHomePath } from "@/config/routes";
import { useRouter } from "next/navigation";
import { useCallback, useState } from "react";
import { Button } from "@/components/ui/button";
import { GoogleIcon } from "@/features/auth/components/google-icon";
import { useGoogleIdentity } from "@/features/auth/hooks/use-google-identity";

export function GoogleSignInButton({
  role,
  onNeedsRole,
  label = "Continue with Google",
  size = "md",
}: {
  role?: UserRole;
  onNeedsRole?: (idToken: string) => void;
  label?: string;
  size?: "sm" | "md";
}) {
  const router = useRouter();
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  const handleCredential = useCallback(
    async (idToken: string, selectedRole?: UserRole) => {
      setError("");
      setPending(true);
      try {
        const { user, needsEmployerOnboarding } = await googleAuthRequest(idToken, selectedRole ?? role);
        router.replace(
          getHomePath(user.role, user.accountStatus, {
            cin: user.cin,
            needsEmployerOnboarding,
          }),
        );
        router.refresh();
      } catch (err) {
        if (err instanceof AuthRequestError && err.code === "NEEDS_ROLE") {
          onNeedsRole?.(idToken);
          return;
        }
        setError(err instanceof Error ? err.message : "Google sign-in failed.");
      } finally {
        setPending(false);
      }
    },
    [onNeedsRole, role, router],
  );

  const {
    hiddenButtonRef,
    configured,
    configLoading,
    error: loadError,
    ready,
    triggerSignIn,
  } = useGoogleIdentity((idToken) => {
    void handleCredential(idToken);
  });

  function handleClick() {
    setError("");
    if (!configured) {
      setError("Google sign-in is not configured. Set GOOGLE_CLIENT_ID on the backend and restart.");
      return;
    }
    if (!ready) {
      setError("Google sign-in is still loading. Try again in a moment.");
      return;
    }
    triggerSignIn();
  }

  const disabled = pending || configLoading;
  const buttonLabel = pending ? "Signing in..." : configLoading ? "Loading Google..." : label;

  return (
    <div className="space-y-2">
      {configured ? (
        <div ref={hiddenButtonRef} className="fixed top-0 -left-[9999px] h-px w-[280px] overflow-hidden opacity-0" aria-hidden />
      ) : null}
      <Button
        type="button"
        variant="secondary"
        size={size}
        className="w-full"
        disabled={disabled}
        aria-busy={pending || configLoading}
        onClick={handleClick}
      >
        <GoogleIcon className="h-5 w-5 shrink-0" />
        <span>{buttonLabel}</span>
      </Button>
      {loadError || error ? (
        <p role="alert" className="text-sm text-danger">
          {loadError || error}
        </p>
      ) : null}
    </div>
  );
}

export function GoogleRoleRetry({
  idToken,
  onCancel,
}: {
  idToken: string;
  onCancel: () => void;
}) {
  const router = useRouter();
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function continueSignup() {
    setError("");
    setPending(true);
    try {
      const { user, needsEmployerOnboarding } = await googleAuthRequest(idToken, "CANDIDATE");
      router.replace(
        getHomePath(user.role, user.accountStatus, {
          cin: user.cin,
          needsEmployerOnboarding,
        }),
      );
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not finish Google sign-up.");
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="rounded-[var(--radius-control)] border border-input bg-muted/40 p-4">
      <p className="text-sm font-medium text-ink">Finish creating your account</p>
      <p className="mt-1 text-sm text-ink-secondary">
        Google sign-in is for candidate accounts. Continue to create a candidate profile.
      </p>
      {error ? (
        <p role="alert" className="mt-3 text-sm text-danger">
          {error}
        </p>
      ) : null}
      <div className="mt-4 flex flex-col gap-2 sm:flex-row-reverse">
        <button
          type="button"
          disabled={pending}
          onClick={() => void continueSignup()}
          className="inline-flex h-10 items-center justify-center rounded-[var(--radius-control)] bg-primary px-4 text-sm font-semibold text-white disabled:opacity-60"
        >
          {pending ? "Continuing..." : "Continue as candidate"}
        </button>
        <button
          type="button"
          disabled={pending}
          onClick={onCancel}
          className="inline-flex h-10 items-center justify-center rounded-[var(--radius-control)] border border-input px-4 text-sm font-medium text-ink-secondary"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
