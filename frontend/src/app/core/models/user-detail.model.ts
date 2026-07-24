import { UserSummary } from './user-summary.model';

export interface UserDetail extends UserSummary {
  createdAt: string;
  updatedAt: string;
}
