export interface User {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  mobileNumber?: string;
  residentialAddress?: string;
  pan?: string;
  currency: string;
  tradingStyle: 'Day Trader' | 'Swing Trader' | 'Scalper';
  role?: string;
  joinedDate?: string;
}

export interface UserProfileUpdate {
  firstName: string;
  lastName: string;
  mobileNumber?: string;
  residentialAddress?: string;
  pan?: string;
  currency: string;
  tradingStyle: 'Day Trader' | 'Swing Trader' | 'Scalper';
}
