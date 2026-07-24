export interface UserSummary {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  accountNonLocked: boolean;
  roles: string[];
}
