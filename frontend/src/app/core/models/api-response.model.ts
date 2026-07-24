export interface ApiValidationError {
  field?: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string;
  errors?: ApiValidationError[] | null;
}
