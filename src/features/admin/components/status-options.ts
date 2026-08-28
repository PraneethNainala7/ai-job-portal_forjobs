import {
  CheckCircleIcon,
  ClockCounterClockwiseIcon,
  ListDashesIcon,
  LockSimpleIcon,
  PauseCircleIcon,
  ProhibitIcon,
  XCircleIcon,
} from "@phosphor-icons/react";
import type { SelectMenuOption } from "@/components/ui/select-menu";

const all: SelectMenuOption = { value: "ALL", label: "All statuses", icon: ListDashesIcon };

export const candidateStatusOptions: SelectMenuOption[] = [
  all,
  { value: "ACTIVE", label: "Active", icon: CheckCircleIcon },
  { value: "ON_HOLD", label: "On hold", icon: PauseCircleIcon },
  { value: "INACTIVE", label: "Inactive", icon: ProhibitIcon },
];

export const employerStatusOptions: SelectMenuOption[] = [
  all,
  { value: "PENDING", label: "Pending", icon: ClockCounterClockwiseIcon },
  { value: "ACTIVE", label: "Active", icon: CheckCircleIcon },
  { value: "REJECTED", label: "Rejected", icon: XCircleIcon },
  { value: "ON_HOLD", label: "On hold", icon: PauseCircleIcon },
  { value: "INACTIVE", label: "Inactive", icon: ProhibitIcon },
];

export const jobStatusOptions: SelectMenuOption[] = [
  all,
  { value: "ACTIVE", label: "Active", icon: CheckCircleIcon },
  { value: "CLOSED", label: "Closed", icon: LockSimpleIcon },
];
