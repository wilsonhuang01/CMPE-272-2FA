export enum TwoFactorMethod {
  EMAIL = 'EMAIL',
  AUTHENTICATOR_APP = 'AUTHENTICATOR_APP'
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  twoFactorMethod?: TwoFactorMethod;
  isTwoFactorEnabled: boolean;
}

export interface AuthResponse {
  token?: string;
  type?: string;
  id?: number;
  email?: string;
  firstName?: string;
  lastName?: string;
  twoFactorMethod?: TwoFactorMethod;
  isTwoFactorEnabled?: boolean;
  requiresTwoFactor?: boolean;
  message?: string;
}

export interface SignupData {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  twoFactorMethod?: TwoFactorMethod;
}

export interface LoginData {
  email: string;
  password: string;
  twoFactorCode?: string;
}

export interface VerificationData {
  email: string;
  code: string;
}
