"use client";

import { useCallback, useEffect, useRef, useState } from "react";

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
            auto_select?: boolean;
            use_fedcm_for_prompt?: boolean;
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: {
              type?: "standard" | "icon";
              theme?: "outline" | "filled_blue" | "filled_black";
              size?: "large" | "medium" | "small";
              text?: "signin_with" | "signup_with" | "continue_with" | "signin";
              width?: number;
            },
          ) => void;
          prompt: () => void;
        };
      };
    };
  }
}

const GIS_SCRIPT = "https://accounts.google.com/gsi/client";

let scriptPromise: Promise<void> | null = null;

function loadGoogleScript() {
  if (typeof window === "undefined") {
    return Promise.reject(new Error("Google sign-in is unavailable."));
  }
  if (window.google?.accounts?.id) {
    return Promise.resolve();
  }
  if (scriptPromise) {
    return scriptPromise;
  }
  scriptPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector(`script[src="${GIS_SCRIPT}"]`);
    if (existing) {
      existing.addEventListener("load", () => resolve());
      existing.addEventListener("error", () => reject(new Error("Could not load Google sign-in.")));
      return;
    }
    const script = document.createElement("script");
    script.src = GIS_SCRIPT;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Could not load Google sign-in."));
    document.head.appendChild(script);
  });
  return scriptPromise;
}

function stripEnvQuotes(value?: string) {
  const trimmed = value?.trim() ?? "";
  if (
    trimmed.length >= 2 &&
    ((trimmed.startsWith('"') && trimmed.endsWith('"')) ||
      (trimmed.startsWith("'") && trimmed.endsWith("'")))
  ) {
    return trimmed.slice(1, -1).trim();
  }
  return trimmed;
}

function readEnvClientId() {
  return stripEnvQuotes(process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID);
}

async function fetchGoogleClientId(): Promise<string> {
  const envClientId = readEnvClientId();
  try {
    const response = await fetch("/api/auth/google-config");
    if (!response.ok) {
      return envClientId;
    }
    const body = (await response.json()) as { clientId?: string | null; enabled?: boolean };
    if (body.clientId?.trim()) {
      return stripEnvQuotes(body.clientId);
    }
  } catch {
    // Fall back to build-time env when API is unavailable.
  }
  return envClientId;
}

export function useGoogleIdentity(onCredential: (idToken: string) => void) {
  const hiddenButtonRef = useRef<HTMLDivElement>(null);
  const [clientId, setClientId] = useState("");
  const [configLoading, setConfigLoading] = useState(true);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState("");
  const callbackRef = useRef(onCredential);

  useEffect(() => {
    callbackRef.current = onCredential;
  }, [onCredential]);

  useEffect(() => {
    let cancelled = false;
    setConfigLoading(true);
    fetchGoogleClientId()
      .then((id) => {
        if (!cancelled) {
          setClientId(id);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setConfigLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!clientId) {
      setReady(false);
      return;
    }
    let cancelled = false;
    loadGoogleScript()
      .then(() => {
        if (cancelled || !window.google?.accounts?.id) return;
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: (response) => callbackRef.current(response.credential),
          use_fedcm_for_prompt: false,
        });
        setReady(true);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Could not load Google sign-in.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [clientId]);

  useEffect(() => {
    if (!ready || !hiddenButtonRef.current || !window.google?.accounts?.id) return;
    hiddenButtonRef.current.innerHTML = "";
    window.google.accounts.id.renderButton(hiddenButtonRef.current, {
      type: "standard",
      theme: "outline",
      size: "large",
      text: "continue_with",
      width: 280,
    });
  }, [ready]);

  const triggerSignIn = useCallback(() => {
    if (!window.google?.accounts?.id) return;
    const googleBtn = hiddenButtonRef.current?.querySelector('div[role="button"]');
    if (googleBtn instanceof HTMLElement) {
      googleBtn.click();
      return;
    }
    window.google.accounts.id.prompt();
  }, []);

  return {
    hiddenButtonRef,
    clientId,
    configured: Boolean(clientId),
    configLoading,
    error,
    ready,
    triggerSignIn,
  };
}
